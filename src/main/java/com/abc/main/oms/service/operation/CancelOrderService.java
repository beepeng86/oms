package com.abc.main.oms.service.operation;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.order.CancelOrder;
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
public class CancelOrderService implements OrderOperationService {
    private final OpenOrdersService openOrdersService;
    private final AccountBalanceService accountBalanceService;
    private final InventoryService inventoryService;
    @Override
    public OrderCommand onProposal(OrderProposal proposal) {
        OpenOrder openOrder = openOrdersService.createOpenOrder(proposal);

        openOrdersService.printAll(openOrder.getKey());
        return new CancelOrder(proposal);
    }

    @Override
    public void onSuccess(OpenOrder cancelOrder, OrderCommandResult order) {
        OpenOrder createOrder = openOrdersService.findCorrespondingCreateOrder(cancelOrder)
                .orElseThrow(() -> new IllegalStateException("Create Order not found for: " + cancelOrder.getKey()));

        inventoryService.revertFrozenInventoryOnCancel(createOrder);
        accountBalanceService.revertBalanceOnCancel(createOrder);
        openOrdersService.removeOrder(createOrder);

        openOrdersService.removeOrder(cancelOrder);
    }

    @Override
    public void onFail(OpenOrder openOrder, OrderCommandResult order) {
        openOrdersService.removeOrder(openOrder);
    }
}
