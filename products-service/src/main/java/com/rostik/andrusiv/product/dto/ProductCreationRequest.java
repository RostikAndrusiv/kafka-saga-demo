package com.rostik.andrusiv.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ProductCreationRequest {
    @NotBlank
    private String name;
    @NotNull
    @Positive
    private BigDecimal price;
    @Positive
    private Integer quantity;
}
