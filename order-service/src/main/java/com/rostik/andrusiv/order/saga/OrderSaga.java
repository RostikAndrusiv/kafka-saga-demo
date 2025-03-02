package com.rostik.andrusiv.order.saga;

import com.rostik.andrusiv.core.dto.command.*;
import com.rostik.andrusiv.core.dto.event.*;
import com.rostik.andrusiv.core.exception.NotRetryableException;
import com.rostik.andrusiv.core.type.OrderStatus;
import com.rostik.andrusiv.order.jpa.entity.ProcessedEventEntity;
import com.rostik.andrusiv.order.jpa.repository.ProcessedEventRepository;
import com.rostik.andrusiv.order.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.rostik.andrusiv.core.helper.Constants.MESSAGE_ID;

@Component
@KafkaListener(topics = {"${order.events.topic.name}",
                        "${product.evants.topic.name}",
                        "${payment.events.topic.name}"
})
@RequiredArgsConstructor
public class OrderSaga {

    @Value("${product.commands.topic.name}")
    private String productsCommandTopicName;

    @Value("${payment.commands.topic.name}")
    private String paymentCommandsTopicName;

    @Value("${order.commands.topic.name}")
    private String orderCommandsTopicName;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderHistoryService orderHistoryService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaHandler
    @Transactional
    public void handleOrderCreatedEvent(@Payload OrderCreatedEvent orderCreatedEvent,
                                        @Header(MESSAGE_ID) String messageId,
                                        @Header(KafkaHeaders.RECEIVED_KEY) String receivedKey) {

        if (processedEventRepository.findByMessageId(messageId).isPresent()){
            return;
        }
        ReserveProductCommand reserveProductCommand = new ReserveProductCommand();
        BeanUtils.copyProperties(orderCreatedEvent, reserveProductCommand);
        String messageKey = orderCreatedEvent.getOrderId().toString();
        kafkaTemplate.send(productsCommandTopicName, messageKey, reserveProductCommand);
        orderHistoryService.add(orderCreatedEvent.getOrderId(), OrderStatus.CREATED);
        try {
            processedEventRepository.save(new ProcessedEventEntity(messageId, receivedKey));
        } catch (DataIntegrityViolationException e) {
            throw new NotRetryableException(e.getMessage());
        }
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
    public void handleProductReservationFailedEvent(@Payload ProductReservationFailedEvent event) {
        CancelProductReservationCommand command = new CancelProductReservationCommand(
                event.getProductId(), event.getOrderId(), event.getProductQuantity());
        String messageKey = event.getOrderId().toString();
        kafkaTemplate.send(productsCommandTopicName, messageKey, command);
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
