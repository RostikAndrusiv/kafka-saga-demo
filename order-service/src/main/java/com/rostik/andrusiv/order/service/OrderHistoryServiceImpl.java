package com.rostik.andrusiv.order.service;

import com.rostik.andrusiv.core.type.OrderStatus;
import com.rostik.andrusiv.order.dto.OrderHistory;
import com.rostik.andrusiv.order.jpa.entity.OrderHistoryEntity;
import com.rostik.andrusiv.order.jpa.repository.OrderHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderHistoryServiceImpl implements OrderHistoryService {
    private final OrderHistoryRepository orderHistoryRepository;

    public OrderHistoryServiceImpl(OrderHistoryRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Override
    public void add(UUID orderId, OrderStatus orderStatus) {
        log.info("Adding order history for Order ID: {} with Status: {}", orderId, orderStatus);
        OrderHistoryEntity entity = new OrderHistoryEntity();
        entity.setOrderId(orderId);
        entity.setStatus(orderStatus);
        entity.setCreatedAt(new Timestamp(new Date().getTime()));
        orderHistoryRepository.save(entity);
        log.info("Order history added successfully for Order ID: {}", orderId);
    }

    @Override
    public List<OrderHistory> findByOrderId(UUID orderId) {
        log.info("Fetching order history for Order ID: {}", orderId);
        var entities = orderHistoryRepository.findByOrderId(orderId);
        List<OrderHistory> orderHistories = entities.stream().map(entity -> {
            OrderHistory orderHistory = new OrderHistory();
            BeanUtils.copyProperties(entity, orderHistory);
            return orderHistory;
        }).toList();
        log.info("Found {} history entries for Order ID: {}", orderHistories.size(), orderId);
        return orderHistories;
    }
}
