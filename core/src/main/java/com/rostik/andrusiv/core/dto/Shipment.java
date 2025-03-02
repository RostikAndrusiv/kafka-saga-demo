package com.rostik.andrusiv.core.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    private UUID id;
    private UUID orderId;
    private UUID paymentId;

    public Shipment(UUID orderId, UUID paymentId) {
        this.orderId = orderId;
        this.paymentId = paymentId;
    }
}
