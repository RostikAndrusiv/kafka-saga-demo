package com.rostik.andrusiv.order.service;

import com.rostik.andrusiv.core.dto.Order;
import com.rostik.andrusiv.core.dto.event.OrderApprovedEvent;
import com.rostik.andrusiv.core.dto.event.OrderCreatedEvent;
import com.rostik.andrusiv.core.exception.OrderNotFoundException;
import com.rostik.andrusiv.core.type.OrderStatus;
import com.rostik.andrusiv.order.jpa.entity.OrderEntity;
import com.rostik.andrusiv.order.jpa.repository.OrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        OrderEntity entity = new OrderEntity();
        BeanUtils.copyProperties(order, entity);
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);

        sendPlacedOrderEvent(order, entity);

        Order result = new Order();
        BeanUtils.copyProperties(entity, result);
        result.setOrderId(entity.getId());
        return result;
    }

    @Override
    @Transactional
    public void approveOrder(UUID orderId) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(()-> new OrderNotFoundException("Order not found"));
        entity.setStatus(OrderStatus.APPROVED);
        orderRepository.save(entity);
        OrderApprovedEvent orderApprovedEvent= new OrderApprovedEvent(orderId);
        kafkaTemplate.send(ordersEventTopicName, orderApprovedEvent);

    }

    @Override
    @Transactional
    public void rejectOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        order.setStatus(OrderStatus.REJECTED);
    }

    private void sendPlacedOrderEvent(Order order, OrderEntity entity) {
        OrderCreatedEvent placedOrder = new OrderCreatedEvent(
                entity.getId(),
                entity.getCustomerId(),
                order.getProductId(),
                order.getProductQuantity()
        );

        kafkaTemplate.send(ordersEventTopicName, placedOrder);
    }
}
