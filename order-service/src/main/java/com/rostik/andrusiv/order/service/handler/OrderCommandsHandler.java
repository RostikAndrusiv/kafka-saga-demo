package com.rostik.andrusiv.order.service.handler;

import com.rostik.andrusiv.core.dto.command.ApproveOrderCommand;
import com.rostik.andrusiv.core.dto.command.RejectOrderCommand;
import com.rostik.andrusiv.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = {"${order.commands.topic.name}"})
public class OrderCommandsHandler {

    private final OrderService orderService;

    @KafkaHandler
    public void handleApproveOrderCommand(@Payload ApproveOrderCommand command) {
        log.info("Received ApproveOrderCommand for Order ID: {}", command.getOrderId());
        orderService.approveOrder(command.getOrderId());
        log.info("Order ID: {} approved successfully.", command.getOrderId());
    }

    @KafkaHandler
    public void handleRejectOrderCommand(@Payload RejectOrderCommand command) {
        log.info("Received RejectOrderCommand for Order ID: {}", command.getOrderId());
        orderService.rejectOrder(command.getOrderId());
        log.info("Order ID: {} rejected successfully.", command.getOrderId());
    }
}
