package com.rostik.andrusiv.core.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer quantity;

    public Product(UUID productId, Integer quantity) {
        this.id = productId;
        this.quantity = quantity;
    }
}
