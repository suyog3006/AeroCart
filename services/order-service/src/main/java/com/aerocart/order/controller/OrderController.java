package com.aerocart.order.controller;

import com.aerocart.order.dto.CreateOrderRequest;
import com.aerocart.order.model.Order;
import com.aerocart.order.repository.OrderRepository;
import com.aerocart.order.saga.SagaOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final SagaOrchestrator sagaOrchestrator;
    private final OrderRepository orderRepository;

    public OrderController(SagaOrchestrator sagaOrchestrator, OrderRepository orderRepository) {
        this.sagaOrchestrator = sagaOrchestrator;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CreateOrderRequest request) {
        Order order = sagaOrchestrator.executeOrderSaga(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity(),
                request.getAmount()
        );
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        return orderRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderRepository.findByUserId(userId));
    }
}
