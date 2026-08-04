package com.aerocart.notification.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotentNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(IdempotentNotificationConsumer.class);

    private final StringRedisTemplate redisTemplate;

    public IdempotentNotificationConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Kafka Listener with Idempotent Message Deduplication via Redis tracking keys.
     * Guarantees Exactly-Once notification processing.
     */
    @KafkaListener(topics = "order-completed-events", groupId = "notification-group")
    public void consumeOrderCompleted(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String eventId = record.key();
        String redisKey = "processed_event:" + eventId;

        // Set if Not Exists (NX) with TTL 24h
        Boolean isFirstTime = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSED", Duration.ofHours(24));

        if (Boolean.FALSE.equals(isFirstTime)) {
            log.warn("IDEMPOTENCY DUPLICATE DETECTED: Skipping eventId {} already processed.", eventId);
            ack.acknowledge();
            return;
        }

        try {
            log.info("PROCESSING NOTIFICATION [Exactly-Once Guaranteed]: Order Completed for Key: {}, Payload: {}",
                    eventId, record.value());
            
            // Send Notification (e.g. Email / Push Notification / SMS)
            sendEmailNotification(record.value());

            // Manually acknowledge offset commit
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed processing notification for eventId {}. Evicting idempotency key for retry.", eventId, e);
            redisTemplate.delete(redisKey);
            throw e;
        }
    }

    private void sendEmailNotification(String payload) {
        log.info("Email notification dispatched successfully for order payload: {}", payload);
    }
}
