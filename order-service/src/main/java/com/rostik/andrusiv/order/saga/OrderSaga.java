package com.rostik.andrusiv.order.saga;

import com.rostik.andrusiv.core.dto.command.*;
import com.rostik.andrusiv.core.dto.event.*;
import com.rostik.andrusiv.core.type.OrderStatus;
import com.rostik.andrusiv.order.service.OrderHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"${order.events.topic.name}",
                        "${product.evants.topic.name}",
                        "${payment.events.topic.name}"
})
public class OrderSaga {

    @Value("${product.commands.topic.name}")
    private String productsCommandTopicName;

    @Value("${payment.commands.topic.name}")
    private String paymentCommandsTopicName;

    @Value("${order.commands.topic.name}")
    private String orderCommandsTopicName;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderHistoryService orderHistoryService;

    public OrderSaga(KafkaTemplate<String, Object> kafkaTemplate, OrderHistoryService orderHistoryService) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handleOrderCreatedEvent(@Payload OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand reserveProductCommand = new ReserveProductCommand();
        BeanUtils.copyProperties(orderCreatedEvent, reserveProductCommand);
        String messageKey = orderCreatedEvent.getOrderId().toString();
        kafkaTemplate.send(productsCommandTopicName, messageKey, reserveProductCommand);
        orderHistoryService.add(orderCreatedEvent.getOrderId(), OrderStatus.CREATED);
    }

    @KafkaHandler
    public void handleProductReservedEvent(@Payload ProductReservedEvent event) {
        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(
                event.getOrderId(), event.getProductId(), event.getProductPrice(), event.getProductQuantity());
        String messageKey = event.getOrderId().toString();
        kafkaTemplate.send(paymentCommandsTopicName, messageKey, processPaymentCommand);
    }

    @KafkaHandler
    public void handlePaymentProcessedEventEvent(@Payload PaymentProcessedEvent event) {
        ApproveOrderCommand command = new ApproveOrderCommand(event.getOrderId());
        String messageKey = event.getOrderId().toString();
        kafkaTemplate.send(orderCommandsTopicName, messageKey, command);
    }

    @KafkaHandler
    public void handleOrderApprovedEvent(@Payload OrderApprovedEvent event) {
        orderHistoryService.add(event.getOrderId(), OrderStatus.APPROVED);
    }

    @KafkaHandler
    public void handlePaymentFailedEvent(@Payload PaymentFailedEvent event) {
        CancelProductReservationCommand command = new CancelProductReservationCommand(
                event.getProductId(), event.getOrderId(), event.getProductQuantity());
        String messageKey = event.getOrderId().toString();
        kafkaTemplate.send(productsCommandTopicName, messageKey, command);
    }

    @KafkaHandler
    public void handleProductReservationCancelledEvent(@Payload ProductReservationCancelledEvent event) {
        RejectOrderCommand command = new RejectOrderCommand(event.getOrderId());
        String messageKey = event.getOrderId().toString();
        kafkaTemplate.send(orderCommandsTopicName, messageKey, command);
        orderHistoryService.add(event.getOrderId(), OrderStatus.REJECTED);
    }
}
