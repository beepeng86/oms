package com.abc.main.common.model.order;

import com.abc.main.common.model.OrderProposal;
import lombok.Getter;

@Getter
public abstract class OrderCommand {
    protected final OrderProposal order;
    protected OrderCommand(OrderProposal order) {
        this.order = order;
    }
}
