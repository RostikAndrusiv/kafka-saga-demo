package com.rostik.andrusiv.order.service;

import com.rostik.andrusiv.core.dto.Order;
import com.rostik.andrusiv.core.dto.event.OrderApprovedEvent;
import com.rostik.andrusiv.core.dto.event.OrderCreatedEvent;
import com.rostik.andrusiv.core.exception.OrderNotFoundException;
import com.rostik.andrusiv.core.type.OrderStatus;
import com.rostik.andrusiv.order.jpa.entity.OrderEntity;
import com.rostik.andrusiv.order.jpa.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.rostik.andrusiv.core.helper.Constants.MESSAGE_ID;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final String ordersEventTopicName;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderServiceImpl(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate,
                            @Value("${order.events.topic.name}") String ordersEventTopicName) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.ordersEventTopicName = ordersEventTopicName;
    }

    @Override
    public Order placeOrder(Order order) {
        log.info("Placing order for Customer ID: {} with Product ID: {} and Quantity: {}",
                order.getCustomerId(), order.getProductId(), order.getProductQuantity());

        OrderEntity entity = new OrderEntity();
        BeanUtils.copyProperties(order, entity);
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);

        sendPlacedOrderEvent(order, entity);

        Order result = new Order();
        BeanUtils.copyProperties(entity, result);
        result.setOrderId(entity.getId());

        log.info("Order placed successfully with Order ID: {}", result.getOrderId());
        return result;
    }

    @Override
    @Transactional
    public void approveOrder(UUID orderId) {
        log.info("Approving order with Order ID: {}", orderId);

        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        entity.setStatus(OrderStatus.APPROVED);
        orderRepository.save(entity);

        OrderApprovedEvent orderApprovedEvent = new OrderApprovedEvent(orderId);
        String messageKey = orderId.toString();
        kafkaTemplate.send(ordersEventTopicName, messageKey, orderApprovedEvent);

        log.info("Order with Order ID: {} approved and event sent to Kafka", orderId);
    }

    @Override
    @Transactional
    public void rejectOrder(UUID orderId) {
        log.info("Rejecting order with Order ID: {}", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);

        log.info("Order with Order ID: {} rejected", orderId);
    }

    private void sendPlacedOrderEvent(Order order, OrderEntity entity) {
        log.info("Sending placed order event for Order ID: {}", entity.getId());

        OrderCreatedEvent placedOrder = new OrderCreatedEvent(
                entity.getId(),
                entity.getCustomerId(),
                order.getProductId(),
                order.getProductQuantity()
        );

        String messageKey = entity.getId().toString();
        ProducerRecord<String, Object> record = new ProducerRecord<>(ordersEventTopicName, messageKey, placedOrder);
        record.headers().add(MESSAGE_ID, UUID.randomUUID().toString().getBytes());

        kafkaTemplate.send(record);

        log.info("Placed order event sent for Order ID: {}", entity.getId());
    }
}
