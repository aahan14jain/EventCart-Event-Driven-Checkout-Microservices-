package com.eventcart.inventory_service.messaging;

import com.eventcart.inventory_service.events.InventoryFailedEvent;
import com.eventcart.inventory_service.events.InventoryReservedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderCreatedListener(ObjectMapper objectMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    public void onOrderCreated(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.has("orderId") ? root.get("orderId").asText() : null;
            if (orderId == null) {
                log.warn("Inventory received event with no orderId: {}", message);
                return;
            }

            if (message.contains("\"sku\":\"FAIL\"") || message.contains("\"sku\": \"FAIL\"")) {
                InventoryFailedEvent event = new InventoryFailedEvent(orderId, "OUT_OF_STOCK");
                kafkaTemplate.send("inventory.failed", orderId, event);
                log.info("Published inventory.failed: orderId={}, reason=OUT_OF_STOCK", orderId);
            } else {
                InventoryReservedEvent event = new InventoryReservedEvent(orderId);
                kafkaTemplate.send("inventory.reserved", orderId, event);
                log.info("Published inventory.reserved: orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process order.created message: {}", e.getMessage());
        }
    }
}
