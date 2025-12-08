package com.abc.main.oms.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.abc.main.common.model.AccountInfo;
import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OpenOrderKey;
import com.abc.main.common.model.OrderOperation;
import com.abc.main.common.model.Position;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.entity.AccountBalance;
import com.abc.main.common.model.entity.Inventory;
import com.abc.main.common.model.entity.InventoryKey;
import com.abc.main.oms.repo.AccountBalanceHCRepo;
import com.abc.main.oms.repo.InventoryHCRepo;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class ReconcilationServiceTest {

  @Autowired
  private ReconcilationService reconcilationService;
  @Autowired
  private OpenOrdersHCService openOrderService;
  @Autowired
  private AccountBalanceHCRepo accountBalanceHCRepo;
  @Autowired
  private InventoryHCRepo inventoryHCRepo;
  @MockBean
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Test
  void testUpdateAccountInfo_withBuyPosition() {

    OpenOrderKey key = OpenOrderKey.builder()
        .accountId("accountTest")
        .instructionId("instID123")
        .symbol("SOLUSDT")
        .build();

    OpenOrder openOrder1 = OpenOrder.builder()
        .orderId("instID123|orderUUID")
        .key(key)
        .originalPrice(BigDecimal.valueOf(123.45))
        .side(Side.Sell)
        .qty(BigDecimal.valueOf(100L))
        .execQty(BigDecimal.valueOf(50L))
        .orderStatus("ACTIVE")
        .operation(OrderOperation.QUERY)
        .build();

    OpenOrder openOrder2 = OpenOrder.builder()
        .orderId("instID123|orderUUID")
        .key(key)
        .originalPrice(BigDecimal.valueOf(123.45))
        .side(Side.Buy)
        .qty(BigDecimal.valueOf(100L))
        .execQty(BigDecimal.valueOf(50L))
        .orderStatus("ACTIVE")
        .operation(OrderOperation.QUERY)
        .build();

    Position pos = Position.builder()
        .symbol("SOLUSDT")
        .side(Side.Buy)
        .size(BigDecimal.valueOf(123.0))
        .build();

    AccountInfo accountInfo = AccountInfo.builder()
        .accountId("accountTest")
        .symbol("SOLUSDT")
        .balance(BigDecimal.valueOf(198765.0))
        .position(pos)
        .orderList(Arrays.asList(openOrder1, openOrder2))
        .build();

    reconcilationService.updateAccountInfo(accountInfo);


    AccountBalance accBalance = accountBalanceHCRepo.findByIdAndLock("accountTest");
    assertThat(accBalance.getFrozenBalance()).isEqualByComparingTo(BigDecimal.valueOf(6172.5));
    assertThat(accBalance.getCashBalance()).isEqualByComparingTo(BigDecimal.valueOf(198765.0));

    Inventory inventory = inventoryHCRepo.findByKeyAndLock(
        InventoryKey.builder()
            .accountId("accountTest")
            .symbol("SOLUSDT")
            .build());
    assertThat(inventory.getQty()).isEqualByComparingTo(BigDecimal.valueOf(123.0));
    assertThat(inventory.getFrozenQty()).isEqualByComparingTo(BigDecimal.valueOf(50));
  }

  @Test
  void testUpdateAccountInfo_withSellPosition() {

    OpenOrderKey key = OpenOrderKey.builder()
        .accountId("accountTest")
        .instructionId("instID123")
        .symbol("SOLUSDT")
        .build();

    OpenOrder openOrder1 = OpenOrder.builder()
        .orderId("instID123|orderUUID")
        .key(key)
        .originalPrice(BigDecimal.valueOf(123.45))
        .side(Side.Sell)
        .qty(BigDecimal.valueOf(100L))
        .execQty(BigDecimal.valueOf(50L))
        .orderStatus("ACTIVE")
        .operation(OrderOperation.QUERY)
        .build();

    OpenOrder openOrder2 = OpenOrder.builder()
        .orderId("instID123|orderUUID")
        .key(key)
        .originalPrice(BigDecimal.valueOf(123.45))
        .side(Side.Buy)
        .qty(BigDecimal.valueOf(100L))
        .execQty(BigDecimal.valueOf(50L))
        .orderStatus("ACTIVE")
        .operation(OrderOperation.QUERY)
        .build();

    Position pos = Position.builder()
        .symbol("SOLUSDT")
        .side(Side.Sell)
        .size(BigDecimal.valueOf(123.0))
        .build();

    AccountInfo accountInfo = AccountInfo.builder()
        .accountId("accountTest")
        .symbol("SOLUSDT")
        .balance(BigDecimal.valueOf(198765.0))
        .position(pos)
        .orderList(Arrays.asList(openOrder1, openOrder2))
        .build();

    reconcilationService.updateAccountInfo(accountInfo);

    AccountBalance accBalance = accountBalanceHCRepo.findByIdAndLock("accountTest");
    assertThat(accBalance.getFrozenBalance()).isEqualByComparingTo(BigDecimal.valueOf(6172.5));
    assertThat(accBalance.getCashBalance()).isEqualByComparingTo(BigDecimal.valueOf(198765.0));

    Inventory inventory = inventoryHCRepo.findByKeyAndLock(
        InventoryKey.builder()
            .accountId("accountTest")
            .symbol("SOLUSDT")
            .build());
    assertThat(inventory.getQty()).isEqualByComparingTo(BigDecimal.valueOf(-123.0));
    assertThat(inventory.getFrozenQty()).isEqualByComparingTo(BigDecimal.valueOf(50));
  }
}