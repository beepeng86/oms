package com.abc.main.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.abc.main.oms.service.operation.CancelOrderService;
import com.abc.main.oms.service.operation.CreateOrderService;
import com.abc.main.oms.service.operation.OrderOperationService;
import com.abc.main.oms.service.operation.SellWithinThreeSecOrderService;
import com.abc.main.oms.service.operation.UpdateOrderService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderOperation {
    CREATE(CreateOrderService.class),
    CANCEL(CancelOrderService.class),
    UPDATE(UpdateOrderService.class),
    QUERY(CreateOrderService.class),
    // Below are sample of non-direct order operation
    SELL_WITHIN_3SEC(SellWithinThreeSecOrderService.class),
    FILL_OR_KILL_2SEC(SellWithinThreeSecOrderService.class);

    @JsonIgnore
    private final Class<? extends OrderOperationService> serviceClass;
}
