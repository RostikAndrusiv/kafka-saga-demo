package com.rostik.andrusiv.core.dto.event;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductReservationCancelledEvent {
    private UUID productId;
    private UUID orderId;
}
