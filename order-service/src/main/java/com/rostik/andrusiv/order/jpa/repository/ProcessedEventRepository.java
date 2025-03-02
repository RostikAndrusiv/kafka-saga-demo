package com.rostik.andrusiv.order.jpa.repository;

import com.rostik.andrusiv.order.jpa.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, Long> {

    Optional<ProcessedEventEntity> findByMessageId(String messageId);
}
