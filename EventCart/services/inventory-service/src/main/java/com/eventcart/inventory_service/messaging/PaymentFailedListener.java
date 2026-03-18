package com.eventcart.inventory_service.messaging;

import com.eventcart.inventory_service.events.InventoryReleasedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentFailedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentFailedListener.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentFailedListener(ObjectMapper objectMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "payment.failed", groupId = "inventory-group")
    public void onPaymentFailed(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.has("orderId") ? root.get("orderId").asText() : null;
            if (orderId == null) {
                log.warn("Payment failed event with no orderId: {}", message);
                return;
            }

            InventoryReleasedEvent event = new InventoryReleasedEvent(orderId);
            kafkaTemplate.send("inventory.released", orderId, event);
            log.info("Published inventory.released: orderId={}", orderId);
        } catch (Exception e) {
            log.error("Failed to process payment.failed: {}", e.getMessage());
        }
    }
}
