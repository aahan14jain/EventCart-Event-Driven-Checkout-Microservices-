package com.eventcart.orderservice.messaging;

import com.eventcart.orderservice.events.OrderCreatedEvent;
import com.eventcart.orderservice.observability.CorrelationId;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private static final String NO_EVENT_ID = "n/a";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            Headers headers = new RecordHeaders();
            String cid = event.getCorrelationId();
            if (cid != null && !cid.isBlank()) {
                headers.add(CorrelationId.KAFKA_HEADER_NAME, cid.getBytes(StandardCharsets.UTF_8));
            }

            ProducerRecord<String, OrderCreatedEvent> record = new ProducerRecord<>(
                    "order.created",
                    null,
                    null,
                    event.getOrderId(),
                    event,
                    headers);

            kafkaTemplate.send(record)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn(
                                    "event processed: eventType={} orderId={} eventId={} result={} correlationId={} error={}",
                                    "order.created",
                                    event.getOrderId(),
                                    NO_EVENT_ID,
                                    "publish_failed",
                                    event.getCorrelationId(),
                                    ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.warn(
                    "event processed: eventType={} orderId={} eventId={} result={} error={}",
                    "order.created",
                    event.getOrderId(),
                    NO_EVENT_ID,
                    "publish_failed",
                    e.getMessage());
        }
    }
}
