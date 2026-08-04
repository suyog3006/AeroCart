package com.aerocart.order.saga;

import com.aerocart.order.kafka.OrderEventProducer;
import com.aerocart.order.model.Order;
import com.aerocart.order.model.OrderStatus;
import com.aerocart.order.repository.OrderRepository;
import com.aerocart.order.sharding.ShardedDatabaseRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class SagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);

    private final OrderRepository orderRepository;
    private final ShardedDatabaseRouter shardRouter;
    private final OrderEventProducer eventProducer;
    private final RestTemplate restTemplate;

    @Value("${services.inventory-url:http://localhost:8082}")
    private String inventoryServiceUrl;

    public SagaOrchestrator(OrderRepository orderRepository,
                            ShardedDatabaseRouter shardRouter,
                            OrderEventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.shardRouter = shardRouter;
        this.eventProducer = eventProducer;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Executes Distributed Transaction via Saga Orchestration Pattern
     */
    public Order executeOrderSaga(Long userId, String productId, int quantity, BigDecimal amount) {
        String orderId = UUID.randomUUID().toString();
        String shardId = shardRouter.resolveShardId(userId);

        // Step 1: Create Order in PENDING state
        Order order = new Order(orderId, userId, productId, quantity, amount, shardId);
        orderRepository.save(order);
        log.info("SAGA STARTED: Order {} initialized in PENDING status on {}", orderId, shardId);

        try {
            // Step 2: Reserve Inventory Action
            boolean reserved = callReserveInventory(productId, quantity);
            if (!reserved) {
                log.warn("SAGA STEP FAILED: Stock reservation failed for order {}", orderId);
                rollbackSaga(order, "INSUFFICIENT_STOCK");
                return order;
            }

            order.setStatus(OrderStatus.INVENTORY_RESERVED);
            orderRepository.save(order);
            log.info("SAGA STEP SUCCESS: Inventory reserved for order {}", orderId);

            // Step 3: Process Payment Action (Simulated payment gate check)
            boolean paymentSuccess = processPayment(userId, amount);
            if (!paymentSuccess) {
                log.warn("SAGA STEP FAILED: Payment failed for order {}", orderId);
                // Trigger Compensating Action: Release Inventory
                callReleaseInventory(productId, quantity);
                rollbackSaga(order, "PAYMENT_FAILED");
                return order;
            }

            // Step 4: Complete Saga
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            log.info("SAGA COMPLETED: Order {} placed successfully!", orderId);

            // Event-driven notification trigger
            eventProducer.publishOrderCompleted(orderId,
                    String.format("{\"orderId\":\"%s\",\"userId\":%d,\"productId\":\"%s\",\"amount\":%.2f}",
                            orderId, userId, productId, amount));

        } catch (Exception e) {
            log.error("SAGA EXCEPTION: Unexpected error during order processing for order {}", orderId, e);
            callReleaseInventory(productId, quantity);
            rollbackSaga(order, "SYSTEM_ERROR: " + e.getMessage());
        }

        return order;
    }

    private void rollbackSaga(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("SAGA COMPENSATED: Order {} cancelled. Reason: {}", order.getOrderId(), reason);
        eventProducer.publishOrderFailed(order.getOrderId(),
                String.format("{\"orderId\":\"%s\",\"reason\":\"%s\"}", order.getOrderId(), reason));
    }

    private boolean callReserveInventory(String productId, int quantity) {
        try {
            String url = inventoryServiceUrl + "/api/v1/inventory/reserve";
            Map<String, Object> body = Map.of("productId", productId, "quantity", quantity);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Failed to contact Inventory Service for reservation", e);
            return false;
        }
    }

    private void callReleaseInventory(String productId, int quantity) {
        try {
            String url = inventoryServiceUrl + "/api/v1/inventory/release";
            Map<String, Object> body = Map.of("productId", productId, "quantity", quantity);
            restTemplate.postForEntity(url, body, Map.class);
            log.info("COMPENSATING ACTION: Released stock for product {} quantity {}", productId, quantity);
        } catch (Exception e) {
            log.error("CRITICAL: Failed compensating action to release stock for product {}", productId, e);
        }
    }

    private boolean processPayment(Long userId, BigDecimal amount) {
        // Business logic: Decline if amount is higher than 10,000 to demonstrate rollback
        return amount.compareTo(new BigDecimal("10000.00")) <= 0;
    }
}
