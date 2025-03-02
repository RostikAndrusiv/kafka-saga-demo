package com.rostik.andrusiv.core.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private UUID id;
    private UUID orderId;
    private UUID productId;
    private BigDecimal productPrice;
    private Integer productQuantity;

    public Payment(UUID orderId, UUID productId, BigDecimal productPrice, Integer productQuantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
    }
}

