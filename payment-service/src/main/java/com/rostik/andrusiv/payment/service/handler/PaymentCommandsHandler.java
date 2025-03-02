package com.rostik.andrusiv.payment.service.handler;

import com.rostik.andrusiv.core.dto.Payment;
import com.rostik.andrusiv.core.dto.command.ProcessPaymentCommand;
import com.rostik.andrusiv.core.dto.event.PaymentFailedEvent;
import com.rostik.andrusiv.core.dto.event.PaymentProcessedEvent;
import com.rostik.andrusiv.payment.service.PaymentService;
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
@RequiredArgsConstructor
@KafkaListener(topics = {"${payment.commands.topic.name}"})
public class PaymentCommandsHandler {

    @Value("${payment.events.topic.name}")
    private String paymentEventsTopicName;

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaHandler
    public void handleProcessPaymentCommand(@Payload ProcessPaymentCommand command) {
        Payment payment = new Payment(command.getOrderId(), command.getProductId(),
                command.getProductPrice(), command.getProductQuantity());
        String messageKey = command.getOrderId().toString();

        try {
            Payment processedPayment = paymentService.process(payment);
            PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(
                    processedPayment.getOrderId(), processedPayment.getId());

            kafkaTemplate.send(paymentEventsTopicName, messageKey, paymentProcessedEvent);
        } catch (Exception e) {
            log.error("Error processing payment {}", command.getProductId(), e);
            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(
                    command.getOrderId(), command.getProductId(), command.getProductQuantity());

            kafkaTemplate.send(paymentEventsTopicName, messageKey, paymentFailedEvent);
        }
    }

}
