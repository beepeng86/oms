package com.abc.main.common.model;


import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record OrderCommandResult(
    String orderId,
    String accountId,
    String instructionId,
    String symbol,
    String brokerOrderId,
    String executionStatus,
    String executionMessage,
    String executionCode,
    long executionTime,
    String executionDetail
) implements OrderInfo {}
