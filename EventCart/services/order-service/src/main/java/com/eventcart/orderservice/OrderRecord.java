package com.eventcart.orderservice;

public class OrderRecord {
    private String orderId;
    private OrderStatus status;
    private CreateOrderRequest request;
    /** Same id as {@code OrderCreatedEvent}; set at creation only. */
    private String correlationId;

    public OrderRecord() {
    }

    public OrderRecord(String orderId, OrderStatus status, CreateOrderRequest request) {
        this(orderId, status, request, null);
    }

    public OrderRecord(String orderId, OrderStatus status, CreateOrderRequest request, String correlationId) {
        this.orderId = orderId;
        this.status = status;
        this.request = request;
        this.correlationId = correlationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public CreateOrderRequest getRequest() {
        return request;
    }

    public void setRequest(CreateOrderRequest request) {
        this.request = request;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
