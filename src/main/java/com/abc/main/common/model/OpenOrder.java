package com.abc.main.common.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class OpenOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 3109412531657922555L;

    private String orderId;

    private OpenOrderKey key;
    private Side side;
    private BigDecimal originalPrice;
    private BigDecimal qty;
    private BigDecimal execQty;
    private String orderStatus;
    private String brokerOrderId;
    private OrderOperation operation;
    private String errorMessage;
}
