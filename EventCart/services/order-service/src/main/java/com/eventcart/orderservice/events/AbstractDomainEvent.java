package com.eventcart.orderservice.events;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Common base for Kafka domain events from order-service. Holds {@code correlationId} once so
 * concrete events (e.g. {@link OrderCreatedEvent}) do not duplicate the field.
 * <p>
 * There is no cross-service shared library in this repo yet; if one is added later, this type can
 * move there and remain the superclass for order saga events.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractDomainEvent {

    /** Generated for the saga (e.g. at order creation) for log/trace alignment across services. */
    private String correlationId;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
