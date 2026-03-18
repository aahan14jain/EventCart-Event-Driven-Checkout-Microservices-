package com.eventcart.orderservice.messaging;

import com.eventcart.orderservice.OrderStatus;
import com.eventcart.orderservice.OrderStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SagaStatusListener {

    private static final Logger log = LoggerFactory.getLogger(SagaStatusListener.class);

    private final ObjectMapper objectMapper;
    private final OrderStore orderStore;

    public SagaStatusListener(ObjectMapper objectMapper, OrderStore orderStore) {
        this.objectMapper = objectMapper;
        this.orderStore = orderStore;
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "order-group")
    public void onInventoryReserved(String message) {
        try {
            log.info("SagaStatusListener raw message [inventory.reserved]: {}", message);
            String orderId = extractOrderId(message);
            log.info("inventory.reserved: extracted orderId={}", orderId);
            if (orderId == null) return;
            orderStore.findById(orderId).ifPresentOrElse(
                record -> {
                    OrderStatus previous = record.getStatus();
                    record.setStatus(OrderStatus.RESERVED);
                    orderStore.save(record);
                    OrderStatus newStatus = record.getStatus();
                    log.info("inventory.reserved: order found, previous={}, new={}, saved to OrderStore", previous, newStatus);
                },
                () -> log.warn("inventory.reserved: order not found in OrderStore for orderId={}", orderId)
            );
        } catch (Exception e) {
            log.error("Failed to process inventory.reserved: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-group")
    public void onInventoryFailed(String message) {
        try {
            log.info("SagaStatusListener raw message [inventory.failed]: {}", message);
            String orderId = extractOrderId(message);
            log.info("inventory.failed: extracted orderId={}", orderId);
            if (orderId == null) return;
            orderStore.findById(orderId).ifPresentOrElse(
                record -> {
                    OrderStatus previous = record.getStatus();
                    record.setStatus(OrderStatus.FAILED);
                    orderStore.save(record);
                    OrderStatus newStatus = record.getStatus();
                    log.info("inventory.failed: order found, previous={}, new={}, saved to OrderStore", previous, newStatus);
                },
                () -> log.warn("inventory.failed: order not found in OrderStore for orderId={}", orderId)
            );
        } catch (Exception e) {
            log.error("Failed to process inventory.failed: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.succeeded", groupId = "order-group")
    public void onPaymentSucceeded(String message) {
        try {
            log.info("SagaStatusListener raw message [payment.succeeded]: {}", message);
            String orderId = extractOrderId(message);
            log.info("payment.succeeded: extracted orderId={}", orderId);
            if (orderId == null) return;
            orderStore.findById(orderId).ifPresentOrElse(
                record -> {
                    OrderStatus previous = record.getStatus();
                    record.setStatus(OrderStatus.CONFIRMED);
                    orderStore.save(record);
                    OrderStatus newStatus = record.getStatus();
                    log.info("payment.succeeded: order found, previous={}, new={}, saved to OrderStore", previous, newStatus);
                },
                () -> log.warn("payment.succeeded: order not found in OrderStore for orderId={}", orderId)
            );
        } catch (Exception e) {
            log.error("Failed to process payment.succeeded: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-group")
    public void onPaymentFailed(String message) {
        try {
            log.info("SagaStatusListener raw message [payment.failed]: {}", message);
            String orderId = extractOrderId(message);
            log.info("payment.failed: extracted orderId={}", orderId);
            if (orderId == null) return;
            orderStore.findById(orderId).ifPresentOrElse(
                record -> {
                    OrderStatus previous = record.getStatus();
                    record.setStatus(OrderStatus.FAILED);
                    orderStore.save(record);
                    OrderStatus newStatus = record.getStatus();
                    log.info("payment.failed: order found, previous={}, new={}, saved to OrderStore", previous, newStatus);
                },
                () -> log.warn("payment.failed: order not found in OrderStore for orderId={}", orderId)
            );
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
