package com.rostik.andrusiv.order.service.handler;

import com.rostik.andrusiv.core.dto.command.ApproveOrderCommand;
import com.rostik.andrusiv.core.dto.command.RejectOrderCommand;
import com.rostik.andrusiv.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = {"${order.commands.topic.name}"})
public class OrderCommandsHandler {

    private final OrderService orderService;

    @KafkaHandler
    public void handleApproveOrderCommand(@Payload ApproveOrderCommand command) {
        orderService.approveOrder(command.getOrderId());
    }

    @KafkaHandler
    public void handleRejectOrderCommand(@Payload RejectOrderCommand command) {
        orderService.rejectOrder(command.getOrderId());
    }
}
