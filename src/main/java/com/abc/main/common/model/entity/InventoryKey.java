package com.abc.main.common.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
@ToString
public class InventoryKey implements Serializable {
    @JsonProperty("accountId")
    private String accountId;
    @JsonProperty("symbol")
    private String symbol;
}
