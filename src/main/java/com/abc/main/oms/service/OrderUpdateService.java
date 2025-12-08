package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderOperation;
import com.abc.main.common.model.OrderStatus;
import com.abc.main.common.model.OrderUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderUpdateService {
    private final OpenOrdersService openOrdersService;
    private final AccountBalanceService accountBalanceService;
    private final InventoryService inventoryService;

    @Transactional
    public void processOrder(OrderUpdate order) {
        // TODO: Put in validation logic and/or further refactor validations
        // validate(order);

        OpenOrder openOrder = openOrdersService.getOpenOrder(order);

        assert OrderOperation.CREATE.equals(openOrder.getOperation())
            || OrderOperation.QUERY.equals(openOrder.getOperation());

        accountBalanceService.adjustBalance(order, openOrder.getOriginalPrice());
        inventoryService.adjustInventory(openOrder, order.execQty());

        openOrdersService.updateOpenOrderWithOrderUpdate(openOrder, order);

        // TODO: here we haven't take into account when order update return negative result?
        if (OrderStatus.FILLED.name().equals(openOrder.getOrderStatus())) {
            assert openOrder.getExecQty().compareTo(openOrder.getQty()) == 0;
            openOrdersService.removeOrder(openOrder);
        }
        openOrdersService.printAll(openOrder.getKey());
    }
}
