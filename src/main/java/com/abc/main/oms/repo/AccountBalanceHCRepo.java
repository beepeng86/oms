package com.abc.main.oms.repo;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.abc.main.common.model.entity.AccountBalance;
import com.abc.main.oms.config.OmsKafkaProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBalanceHCRepo {
  private final HazelcastInstance hazelcastInstance;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final OmsKafkaProperties omsKafkaProperties;
  IMap<String, AccountBalance> accountBalanceList;
  
  @PostConstruct
  public void init() {
    accountBalanceList = hazelcastInstance.getMap("AccountBalance");
  }

  public AccountBalance findByIdAndLock(String accountId) {
    lockAccountBalance(accountId);
    AccountBalance accountBalance = accountBalanceList.get(accountId);

    if (accountBalance == null) {
      throw new IllegalArgumentException("AccountBalance not found for id: " + accountId);
    }

    return accountBalance;
  }

  private void lockAccountBalance(String accountId) {
    log.debug("Lock accountBalance: {}", accountId);
    accountBalanceList.lock(accountId);
  }

  public void save(AccountBalance accountBalance) {
    log.debug("AccountBalance: {}", accountBalance);
    accountBalanceList.put(accountBalance.getAccountId(), accountBalance);
    kafkaTemplate.send(
        omsKafkaProperties.topics().get("persist").name(),
        accountBalance.getAccountId(),
        accountBalance);
  }

  public void unlock(AccountBalance accountBalance) {
    log.debug("Unlock AccountBalance: " + accountBalance.getAccountId());
    accountBalanceList.unlock(accountBalance.getAccountId());
  }
}
