package com.rostik.andrusiv.product.service.handler;

import com.rostik.andrusiv.core.dto.Product;
import com.rostik.andrusiv.core.dto.command.CancelProductReservationCommand;
import com.rostik.andrusiv.core.dto.command.ReserveProductCommand;
import com.rostik.andrusiv.core.dto.command.ProductReservationCancelledEvent;
import com.rostik.andrusiv.core.dto.event.ProductReservationFailedEvent;
import com.rostik.andrusiv.core.dto.event.ProductReservedEvent;
import com.rostik.andrusiv.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = {"${product.commands.topic.name}"})
@RequiredArgsConstructor
public class ProductCommandHandler {

    @Value("${product.events.topic.name}")
    private String productEventsTopicName;
    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaHandler
    public void handleReserveProductCommand(@Payload ReserveProductCommand command) {
        log.info("Received ReserveProductCommand for Product ID: {}, Quantity: {}",
                command.getProductId(), command.getProductQuantity());

        Product desiredProduct = Product.builder()
                .id(command.getProductId())
                .quantity(command.getProductQuantity())
                .build();
        String messageKey = command.getOrderId().toString();

        try {
            // Attempt to reserve the product
            Product reservedProduct = productService.reserve(desiredProduct, command.getProductId());
            log.info("Successfully reserved Product ID: {}, Quantity: {}",
                    reservedProduct.getId(), reservedProduct.getQuantity());

            // Create and send the event if reservation is successful
            ProductReservedEvent productReservedEvent = new ProductReservedEvent(
                    command.getOrderId(), command.getProductId(), reservedProduct.getPrice(),
                    command.getProductQuantity());
            kafkaTemplate.send(productEventsTopicName, messageKey, productReservedEvent);
            log.info("ProductReservedEvent sent for Order ID: {}, Product ID: {}",
                    command.getOrderId(), command.getProductId());
        } catch (Exception e) {
            log.error("Failed to reserve Product ID: {} for Order ID: {}. Error: {}",
                    command.getProductId(), command.getOrderId(), e.getMessage(), e);

            // Create and send the failure event
            ProductReservationFailedEvent productReservationFailedEvent = new ProductReservationFailedEvent(
                    command.getProductId(), command.getOrderId(), command.getProductQuantity());
            kafkaTemplate.send(productEventsTopicName, messageKey, productReservationFailedEvent);
            log.info("ProductReservationFailedEvent sent for Order ID: {}, Product ID: {}",
                    command.getOrderId(), command.getProductId());
        }
    }

    @KafkaHandler
    public void handleCancelProductReservationCommand(@Payload CancelProductReservationCommand command) {
        log.info("Received CancelProductReservationCommand for Product ID: {}, Order ID: {}",
                command.getProductId(), command.getOrderId());

        Product productToCancel = new Product(command.getProductId(), command.getProductQuantity());
        productService.cancelReservation(productToCancel, command.getOrderId());
        log.info("Product reservation canceled for Product ID: {}, Order ID: {}",
                command.getProductId(), command.getOrderId());

        // Create and send the cancellation event
        ProductReservationCancelledEvent productReservationCancelledEvent =
                new ProductReservationCancelledEvent(command.getProductId(), command.getOrderId());

        String messageKey = command.getOrderId().toString();
        kafkaTemplate.send(productEventsTopicName, messageKey, productReservationCancelledEvent);
        log.info("ProductReservationCancelledEvent sent for Order ID: {}, Product ID: {}",
                command.getOrderId(), command.getProductId());
    }
}