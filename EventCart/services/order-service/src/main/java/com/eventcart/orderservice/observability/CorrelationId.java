package com.eventcart.orderservice.observability;

/** Request-scoped correlation id: HTTP header, MDC, and request attribute (see constants). */
public final class CorrelationId {

    public static final String HEADER_NAME = "X-Correlation-Id";
    /** SLF4J MDC key; keep in sync with downstream consumers that re-hydrate from events. */
    public static final String MDC_KEY = "correlationId";
    /** Kafka record header name (same value as {@link #MDC_KEY} and JSON field). */
    public static final String KAFKA_HEADER_NAME = MDC_KEY;
    /** Request attribute name (must be a compile-time constant for {@code @RequestAttribute}). */
    public static final String REQUEST_ATTRIBUTE = "com.eventcart.correlationId";

    private CorrelationId() {
    }
}
