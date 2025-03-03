package com.rostik.andrusiv.order.service;


import com.rostik.andrusiv.core.dto.Order;
import com.rostik.andrusiv.order.dto.CreateOrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order placeOrder(Order order);

    void approveOrder(UUID orderId);

    void rejectOrder(UUID orderId);

    List<CreateOrderResponse> findAllOrders();
}
