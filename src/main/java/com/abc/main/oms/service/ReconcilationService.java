package com.abc.main.oms.service;

import com.abc.main.common.model.AccountInfo;
import com.abc.main.common.model.AccountInfoRequestParam;
import com.abc.main.common.model.OpenOrderKey;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.entity.AccountBalance;
import com.abc.main.common.model.entity.Inventory;
import com.abc.main.common.model.entity.InventoryKey;
import com.abc.main.oms.config.OmsKafkaProperties;
import com.abc.main.oms.repo.AccountBalanceHCRepo;
import com.abc.main.oms.repo.InventoryHCRepo;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconcilationService {

  private final OmsKafkaProperties omsKafkaProperties;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final InventoryHCRepo inventoryHCRepo;
  private final AccountBalanceHCRepo accountBalanceHCRepo;
  private final OpenOrdersHCService openOrdersHCService;

  @PostConstruct
  public void init() {
    String commandTopic = omsKafkaProperties.topics().get("command").name();
    log.info("Send request to gather the latest inventory/account balance and open orders");
    omsKafkaProperties.accounts().forEach((k, v) -> {
      AccountInfoRequestParam param = AccountInfoRequestParam.builder()
          .operation("getAccountInfo")
          .symbol(omsKafkaProperties.symbol())
          .accountType(v.accountType())
          .category(v.category())
          .build();
      kafkaTemplate.send(commandTopic, v.partitionId(), k, param);
    });
  }

  public void updateAccountInfo(AccountInfo accountInfo) {
    log.info("Received account info: {}", accountInfo);

    // TODO: get and set instruction id
    accountInfo.orderList()
        .forEach(openOrder -> openOrder.getKey().setInstructionId("inst123"));

    BigDecimal frozenBalance = accountInfo.orderList()
        .stream()
        .filter(openOrder -> Side.Buy.equals(openOrder.getSide()))
        .map(openOrder -> openOrder.getQty()
            .subtract(openOrder.getExecQty())
            .multiply(openOrder.getOriginalPrice()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.debug("Frozen balance: {}", frozenBalance);

    BigDecimal frozenQty = accountInfo.orderList()
        .stream()
        .filter(openOrder -> Side.Sell.equals(openOrder.getSide()))
        .map(openOrder -> openOrder.getQty()
            .subtract(openOrder.getExecQty()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    log.debug("Frozen quantity: {}", frozenQty);

    accountInfo.orderList().forEach(openOrdersHCService::update);
    openOrdersHCService.printAll(
        OpenOrderKey.builder()
            .accountId(accountInfo.accountId())
            .symbol(accountInfo.symbol())
            .instructionId("inst123")
            .build()
    );

    AccountBalance accountBalance = AccountBalance.builder()
        .accountId(accountInfo.accountId())
        .cashBalance(accountInfo.balance())
        .frozenBalance(frozenBalance)
        .build();

    accountBalanceHCRepo.save(accountBalance);

    BigDecimal positionSize = accountInfo.position().size();

    if(Objects.equals(Side.Sell, accountInfo.position().side())) {
      positionSize = positionSize.multiply(BigDecimal.valueOf(-1)) ;
    }

    Inventory inventory = Inventory.builder()
        .id(InventoryKey.builder()
            .accountId(accountInfo.accountId())
            .symbol(accountInfo.position().symbol())
            .build())
        .qty(positionSize)
        .frozenQty(frozenQty)
        .build();

    inventoryHCRepo.save(inventory);
  }
}
