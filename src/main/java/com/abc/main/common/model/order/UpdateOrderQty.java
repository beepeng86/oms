package com.abc.main.common.model.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.abc.main.common.model.OrderProposal;

public class UpdateOrderQty extends OrderCommand {
    public UpdateOrderQty(@JsonProperty("order") OrderProposal order) {
        super(order);
    }

    @Override
    public String toString() {
        return "UpdateOrderQty{" +
                "order=" + order +
                '}';
    }
}
