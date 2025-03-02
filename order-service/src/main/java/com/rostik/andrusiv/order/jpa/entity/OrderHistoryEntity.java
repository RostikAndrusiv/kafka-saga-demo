package com.rostik.andrusiv.order.jpa.entity;

import com.rostik.andrusiv.core.type.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Table(name = "order_history")
@Entity
@Getter
@Setter
public class OrderHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "status")
    private OrderStatus status;
    @Column(name = "created_at")
    private Timestamp createdAt;
}
