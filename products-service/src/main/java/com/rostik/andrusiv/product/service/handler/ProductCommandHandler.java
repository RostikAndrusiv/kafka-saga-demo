package com.rostik.andrusiv.product.service.handler;

import com.rostik.andrusiv.core.dto.Product;
import com.rostik.andrusiv.core.dto.command.ReserveProductCommand;
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
        Product desiredProduct = Product.builder()
                .id(command.getProductId())
                .quantity(command.getProductQuantity())
                .build();

        try {
            Product reservedProduct = productService.reserve(desiredProduct, command.getProductId());
            ProductReservedEvent productReservedEvent = new ProductReservedEvent(
                    command.getOrderId(), command.getProductId(), reservedProduct.getPrice(),
                    command.getProductQuantity());
            kafkaTemplate.send(productEventsTopicName, productReservedEvent);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ProductReservationFailedEvent productReservationFailedEvent = new ProductReservationFailedEvent(
                    command.getProductId(), command.getOrderId(), command.getProductQuantity());
            kafkaTemplate.send(productEventsTopicName, productReservationFailedEvent);
        }
    }
}
