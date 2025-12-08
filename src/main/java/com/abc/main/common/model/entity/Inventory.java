package com.abc.main.common.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
@Setter
@Getter
@ToString
public class Inventory implements Serializable {

    private InventoryKey id;
    private BigDecimal qty;
    private BigDecimal frozenQty;

}
