package com.abc.main.oms.repo;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OpenOrderKey;
import com.abc.main.common.model.OrderOperation;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenOrdersHCRepo {

  private final HazelcastInstance hazelcastInstance;

  public IMap<String, OpenOrder> getOpenOrders(String key) {
    return hazelcastInstance.getMap(key);
  }

  public OpenOrder findByKeyAndOrderId(String key, String orderId) {
    return getOpenOrders(key).get(orderId);
  }

  public void save(OpenOrder openOrder) {
    getOpenOrders(openOrder.getKey().toString())
        .put(openOrder.getOrderId(), openOrder);
  }

  public void remove(OpenOrder openOrder) {
    getOpenOrders(openOrder.getKey().toString())
        .evict(openOrder.getOrderId());
  }
  public void printAll(OpenOrderKey key) {
    IMap<String, OpenOrder> map = hazelcastInstance.getMap(key.toString());

    log.info(formatOrderList(map.values()));
  }

  private String formatOrderList(Collection<OpenOrder> list) {
    StringBuilder sb = new StringBuilder();
    sb.append("%n%-20s%-50s%-40s%-5s%12s%12s%12s%13s%12s%n".formatted(
        "accountId",
        "orderId",
        "brokerId",
        "side",
        "price",
        "qty",
        "execQty",
        "orderStatus",
        "operation"));
    list.forEach(v -> sb.append(
        "%-20s%-50s%-40s%-5s%12s%12s%12s%13s%12s%n".formatted(
            v.getKey().getAccountId(),
            v.getOrderId(),
            v.getBrokerOrderId(),
            v.getSide(),
            v.getOriginalPrice(),
            v.getQty(),
            v.getExecQty(),
            v.getOrderStatus(),
            v.getOperation().name())));
    return sb.toString();
  }

  public Optional<OpenOrder> findCorrespondingCreateOrder(OpenOrder cancelOrder) {
    IMap<String, OpenOrder> map = hazelcastInstance.getMap(cancelOrder.getKey().toString());

    PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
    Predicate predicate = e.get("brokerOrderId").equal(cancelOrder.getBrokerOrderId())
        .and(e.get("operation").equal(OrderOperation.CREATE)
            .or(e.get("operation").equal(OrderOperation.QUERY))
        );

    return Optional.of((OpenOrder) map.values(predicate).toArray()[0]);
  }
}
