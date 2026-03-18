package com.eventcart.notification_service.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SagaNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(SagaNotificationListener.class);

    private final ObjectMapper objectMapper;

    public SagaNotificationListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment.succeeded", groupId = "notification-group")
    public void onPaymentSucceeded(String message) {
        try {
            String orderId = extractOrderId(message);
            if (orderId == null) return;
            log.info("Order confirmed: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to process payment.succeeded: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "inventory.failed", groupId = "notification-group")
    public void onInventoryFailed(String message) {
        try {
            String orderId = extractOrderId(message);
            if (orderId == null) return;
            log.info("Order failed: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to process inventory.failed: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-group")
    public void onPaymentFailed(String message) {
        try {
            String orderId = extractOrderId(message);
            if (orderId == null) return;
            log.info("Order failed: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to process payment.failed: {}", e.getMessage());
        }
    }

    private String extractOrderId(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            return root.has("orderId") ? root.get("orderId").asText() : null;
        } catch (Exception e) {
            log.warn("Could not extract orderId from message: {}", message);
            return null;
        }
    }
}
