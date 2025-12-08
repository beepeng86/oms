package com.abc.main.oms.repo;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.abc.main.oms.config.OmsKafkaProperties;
import com.abc.main.common.model.entity.Inventory;
import com.abc.main.common.model.entity.InventoryKey;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryHCRepo {
  private final HazelcastInstance hazelcastInstance;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final OmsKafkaProperties omsKafkaProperties;
  IMap<String, Inventory> inventoryList;

  @PostConstruct
  public void init() {
    inventoryList = hazelcastInstance.getMap("Inventory");
  }

  public Inventory findByKeyAndLock(InventoryKey key) {
    lockInventory(key.toString());
    Inventory inventory = inventoryList.get(key.toString());

    if (inventory == null) {
      throw new IllegalArgumentException("Inventory not found for key: " + key);
    }

    return inventory;
  }

  private void lockInventory(String inventoryKey) {
    log.debug("Lock inventory: {}", inventoryKey);
    inventoryList.lock(inventoryKey);
  }

  public void save(Inventory inventory) {
    log.debug("Inventory: {}", inventory);
    inventoryList.put(inventory.getId().toString(), inventory);
    kafkaTemplate.send(
        omsKafkaProperties.topics().get("persist").name(),
        inventory.getId().toString(),
        inventory);
  }

  public void unlock(Inventory inventory) {
    log.debug("Unlock inventory: {}", inventory.getId());
    inventoryList.unlock(inventory.getId().toString());
  }
}
