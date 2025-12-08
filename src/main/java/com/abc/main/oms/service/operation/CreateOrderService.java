package com.abc.main.oms.service.operation;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.OrderStatus;
import com.abc.main.common.model.order.CreateOrder;
import com.abc.main.common.model.order.OrderCommand;
import com.abc.main.oms.service.AccountBalanceService;
import com.abc.main.oms.service.InventoryService;
import com.abc.main.oms.service.OpenOrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderService implements OrderOperationService {
    private final OpenOrdersService openOrdersService;
    private final AccountBalanceService accountBalanceService;
    private final InventoryService inventoryService;
    @Override
    public OrderCommand onProposal(OrderProposal proposal) {
        OpenOrder openOrder = openOrdersService.createOpenOrder(proposal);

        inventoryService.freezeInventory(openOrder);
        accountBalanceService.freezeBalance(openOrder);

        openOrdersService.printAll(openOrder.getKey());
        return new CreateOrder(proposal);
    }

    @Override
    public void onSuccess(OpenOrder openOrder, OrderCommandResult order) {
        openOrder.setBrokerOrderId(order.brokerOrderId());
        openOrder.setOrderStatus(OrderStatus.ACTIVE.name());

        openOrdersService.update(openOrder);
    }

    @Override
    public void onFail(OpenOrder openOrder, OrderCommandResult order) {
        openOrder.setErrorMessage(order.executionMessage());
        openOrder.setBrokerOrderId(order.brokerOrderId());
        openOrder.setOrderStatus(OrderStatus.FAILED.name());

        accountBalanceService.revertBalance(openOrder);
        inventoryService.revertFrozenInventory(openOrder);
        openOrdersService.update(openOrder);
    }
}
