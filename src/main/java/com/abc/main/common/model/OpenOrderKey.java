package com.abc.main.common.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Setter
@Getter
public class OpenOrderKey implements Serializable {
    @Serial
    private static final long serialVersionUID = -543282616337279902L;
    private String accountId;
    private String symbol;
    private String instructionId;
}
