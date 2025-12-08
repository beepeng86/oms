package com.abc.main.common.model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record AccountInfoRequestParam(
    String operation,
    String symbol,
    String category,
    String accountType
){}
