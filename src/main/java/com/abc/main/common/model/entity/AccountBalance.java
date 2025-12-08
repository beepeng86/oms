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
public class AccountBalance implements Serializable {

    private String accountId;
    private BigDecimal cashBalance;
    private BigDecimal frozenBalance;

}
