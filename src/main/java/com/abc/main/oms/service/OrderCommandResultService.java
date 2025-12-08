package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderStatus;
import com.abc.main.oms.service.operation.OrderOperationService;
import com.abc.main.oms.service.operation.OrderOperationServiceSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandResultService {
    private final OpenOrdersService openOrdersService;
    private final OrderOperationServiceSelector orderOperationSelector;
    @Transactional
    public void processOrder(OrderCommandResult order) {
        // TODO: Put in validation logic and/or further refactor validations
        // validate(order);

        OpenOrder openOrder = openOrdersService.getOpenOrder(order);
        if (openOrder == null) {
            // In the case where OrderCommandResult return slower than order update, we might not find the open order
            log.info("OpenOrder not found for order: {}", order);
            return;
        }

        assert OrderStatus.PENDING.name().equals(openOrder.getOrderStatus());

        OrderOperationService service = orderOperationSelector.getOrderOperationService(openOrder.getOperation());

        if ("Success".equals(order.executionStatus())) {
            service.onSuccess(openOrder, order);
        } else {
            service.onFail(openOrder, order);
        }
        openOrdersService.printAll(openOrder.getKey());
    }
}
