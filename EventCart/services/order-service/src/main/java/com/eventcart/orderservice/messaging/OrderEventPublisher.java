package com.eventcart.orderservice.messaging;

import com.eventcart.orderservice.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            kafkaTemplate.send("order.created", event.getOrderId(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("Failed to publish OrderCreatedEvent for order {}: {}", event.getOrderId(), ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.warn("Failed to publish OrderCreatedEvent for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
