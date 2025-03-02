package com.rostik.andrusiv.order.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "processed_event")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProcessedEventEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    public ProcessedEventEntity(String messageId, String productId) {
        this.messageId = messageId;
        this.productId = productId;
    }

    @Column(nullable = false)
    private String productId;
}
