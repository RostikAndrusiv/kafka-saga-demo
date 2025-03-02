package com.rostik.andrusiv.order.controller;

import com.rostik.andrusiv.core.dto.Order;
import com.rostik.andrusiv.order.dto.CreateOrderRequest;
import com.rostik.andrusiv.order.dto.CreateOrderResponse;
import com.rostik.andrusiv.order.dto.OrderHistoryResponse;
import com.rostik.andrusiv.order.service.OrderHistoryService;
import com.rostik.andrusiv.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrdersController {
    private final OrderService orderService;
    private final OrderHistoryService orderHistoryService;

    public OrdersController(OrderService orderService, OrderHistoryService orderHistoryService) {
        this.orderService = orderService;
        this.orderHistoryService = orderHistoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateOrderResponse placeOrder(@RequestBody @Valid CreateOrderRequest request) {
        log.info("Placing order for customer: {}", request.getCustomerId());

        var order = new Order();
        BeanUtils.copyProperties(request, order);
        Order createdOrder = orderService.placeOrder(order);

        log.info("Order placed successfully with Order ID: {}", createdOrder.getOrderId());

        var response = new CreateOrderResponse();
        BeanUtils.copyProperties(createdOrder, response);
        return response;
    }

    @GetMapping("/{orderId}/history")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderHistoryResponse> getOrderHistory(@PathVariable UUID orderId) {
        log.info("Retrieving order history for Order ID: {}", orderId);

        List<OrderHistoryResponse> historyResponses = orderHistoryService.findByOrderId(orderId).stream()
                .map(orderHistory -> {
                    OrderHistoryResponse orderHistoryResponse = new OrderHistoryResponse();
                    BeanUtils.copyProperties(orderHistory, orderHistoryResponse);
                    return orderHistoryResponse;
                }).toList();

        log.info("Retrieved {} history entries for Order ID: {}", historyResponses.size(), orderId);
        return historyResponses;
    }
}
