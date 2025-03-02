package com.rostik.andrusiv.core.dto.command;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApproveOrderCommand {
    private UUID orderId;
}
