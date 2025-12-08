package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OpenOrderKey;
import com.abc.main.common.model.OrderInfo;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.OrderStatus;
import com.abc.main.common.model.OrderUpdate;
import com.abc.main.common.model.*;
import com.abc.main.oms.repo.OpenOrdersHCRepo;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("true")
public class OpenOrdersHCService implements OpenOrdersService {
    private final OpenOrdersHCRepo repo;
    private final ConcurrentHashMap<String, String> orderIdToInstIdMap = new ConcurrentHashMap<>();

    @Override
    public void printAll(OpenOrderKey key) {
        log.info("orderIdToInstIdMap: {}", orderIdToInstIdMap);
        repo.printAll(key);
    }

    @Override
    public OpenOrder createOpenOrder(OrderProposal order) {
        OpenOrderKey openOrderKey = OpenOrderKey.builder()
            .accountId(order.accountId())
            .symbol(order.symbol())
            .instructionId(order.instructionId())
            .build();

        OpenOrder newOpenOrder = OpenOrder.builder()
            .orderId(order.orderId())
            .brokerOrderId(order.brokerOrderId())
            .key(openOrderKey)
            .side(order.side())
            .originalPrice(order.price())
            .qty(order.qty())
            .execQty(BigDecimal.ZERO)
            .orderStatus(OrderStatus.PENDING.name())
            .operation(order.orderOperation())
            .build();

        repo.save(newOpenOrder);
        orderIdToInstIdMap.put(order.orderId(), order.instructionId());

        return newOpenOrder;
    }

    @Override
    public OpenOrder getOpenOrder(OrderInfo order) {
        OpenOrderKey openOrderKey = OpenOrderKey.builder()
            .accountId(order.accountId())
            .symbol(order.symbol())
            .instructionId(orderIdToInstIdMap.get(order.orderId()))
            .build();

        return repo.findByKeyAndOrderId(openOrderKey.toString(), order.orderId());
    }

    @Override
    public void update(OpenOrder openOrder) {
        repo.save(openOrder);
    }

    @Override
    public void removeOrder(OpenOrder order) {
        orderIdToInstIdMap.remove(order.getOrderId());
        repo.remove(order);
    }

    @Override
    public void updateOpenOrderWithOrderUpdate(OpenOrder openOrder, OrderUpdate update) {
        // this method is to be used only with CREATE/QUERY order
        if(update.leavesQty().compareTo(BigDecimal.ZERO) == 0) {
            openOrder.setOrderStatus(OrderStatus.FILLED.name());
        } else {
            openOrder.setOrderStatus(OrderStatus.PARTIALLY_FILLED.name());
        }

        openOrder.setExecQty(openOrder.getExecQty().add(update.execQty()));

        repo.save(openOrder);
    }

    @Override
    public Optional<OpenOrder> findCorrespondingCreateOrder(OpenOrder cancelOrder) {
        return repo.findCorrespondingCreateOrder(cancelOrder);
    }
}
