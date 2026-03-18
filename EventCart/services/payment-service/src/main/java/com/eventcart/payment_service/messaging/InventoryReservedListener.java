package com.eventcart.payment_service.messaging;

import com.eventcart.payment_service.events.PaymentFailedEvent;
import com.eventcart.payment_service.events.PaymentSucceededEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservedListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservedListener.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryReservedListener(ObjectMapper objectMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "payment-group")
    public void onInventoryReserved(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String orderId = root.has("orderId") ? root.get("orderId").asText() : null;
            if (orderId == null) {
                log.warn("Payment received event with no orderId: {}", message);
                return;
            }

            boolean shouldFail = orderId.toLowerCase().contains("fail-pay") || orderId.startsWith("PAYFAIL");
            if (shouldFail) {
                PaymentFailedEvent event = new PaymentFailedEvent(orderId, "PAYMENT_DECLINED");
                kafkaTemplate.send("payment.failed", orderId, event);
                log.info("Published payment.failed: orderId={}, reason=PAYMENT_DECLINED", orderId);
            } else {
                PaymentSucceededEvent event = new PaymentSucceededEvent(orderId);
                kafkaTemplate.send("payment.succeeded", orderId, event);
                log.info("Published payment.succeeded: orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process inventory.reserved message: {}", e.getMessage());
        }
    }
}
