package com.abc.main.oms.service.operation;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.order.OrderCommand;
import com.abc.main.common.model.order.UpdateOrderQty;
import com.abc.main.oms.service.AccountBalanceService;
import com.abc.main.oms.service.InventoryService;
import com.abc.main.oms.service.OpenOrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateOrderService implements OrderOperationService {
    private final OpenOrdersService openOrdersService;
    private final AccountBalanceService accountBalanceService;
    private final InventoryService inventoryService;
    @Override
    public OrderCommand onProposal(OrderProposal proposal) {
        // Fill in proper update order logic
        return new UpdateOrderQty(proposal);
    }

    @Override
    public void onSuccess(OpenOrder openOrder, OrderCommandResult order) {
        // Fill in proper update order logic
    }

    @Override
    public void onFail(OpenOrder openOrder, OrderCommandResult order) {
        // Fill in proper update order logic
    }
}
