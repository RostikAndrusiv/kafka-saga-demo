package com.rostik.andrusiv.core.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReserveProductCommand {
    private UUID productId;
    private Integer productQuantity;
    private UUID orderId;
}
