package com.abc.main.common.model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Jacksonized
@Builder
public record OrderProposal (
        String orderId,
        String brokerOrderId,
        String accountId,
        String instructionId,
        String symbol,
        BigDecimal price,
        BigDecimal qty,
        Side side,
        String category,
        String orderType,
        OrderOperation orderOperation
) implements OrderInfo {}
