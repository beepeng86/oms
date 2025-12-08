package com.abc.main.common.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record AccountInfo (
    String accountId,
    String symbol,
    BigDecimal balance,
    Position position,
    List<OpenOrder> orderList)
{}
