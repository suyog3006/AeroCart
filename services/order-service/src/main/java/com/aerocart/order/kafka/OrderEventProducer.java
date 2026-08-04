package com.aerocart.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(String orderId, String payload) {
        log.info("Publishing ORDER_CREATED event for orderId: {}", orderId);
        kafkaTemplate.send("order-created-events", orderId, payload);
    }

    public void publishOrderCompleted(String orderId, String payload) {
        log.info("Publishing ORDER_COMPLETED event for orderId: {}", orderId);
        kafkaTemplate.send("order-completed-events", orderId, payload);
    }

    public void publishOrderFailed(String orderId, String payload) {
        log.info("Publishing ORDER_FAILED event for orderId: {}", orderId);
        kafkaTemplate.send("order-failed-events", orderId, payload);
    }
}
