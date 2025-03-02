package com.rostik.andrusiv.order.service;


import com.rostik.andrusiv.core.dto.Order;

import java.util.UUID;

public interface OrderService {
    Order placeOrder(Order order);
    void approveOrder(UUID orderId);

    void rejectOrder(UUID orderId);
}
