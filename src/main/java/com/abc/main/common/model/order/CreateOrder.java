package com.abc.main.common.model.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.abc.main.common.model.OrderProposal;

public class CreateOrder extends OrderCommand {

    @JsonCreator
    public CreateOrder(@JsonProperty("order") OrderProposal order) {
        super(order);
    }

    @Override
    public String toString() {
        return "CreateOrder{" +
                "order=" + order +
                '}';
    }
}
