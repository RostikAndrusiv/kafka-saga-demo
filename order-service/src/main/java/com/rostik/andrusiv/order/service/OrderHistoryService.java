package com.rostik.andrusiv.order.service;

import com.rostik.andrusiv.core.type.OrderStatus;
import com.rostik.andrusiv.order.dto.OrderHistory;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryService {
    void add(UUID orderId, OrderStatus orderStatus);

    List<OrderHistory> findByOrderId(UUID orderId);
}
