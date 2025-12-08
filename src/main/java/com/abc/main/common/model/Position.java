package com.abc.main.common.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record Position(
    String symbol,
    Side side,
    BigDecimal size
){}
