package com.abc.main.common.model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Jacksonized
@Builder
public record OrderUpdate (
        String orderId,
        String accountId,
        String instructionId,
        String symbol,
        String brokerOrderId,
        String execId,
        BigDecimal price,
        BigDecimal execPrice,
        BigDecimal qty,
        BigDecimal execQty,
        BigDecimal leavesQty,
        BigDecimal execFee,
        Side side,
        String orderStatus,
        String category
) implements OrderInfo {}
