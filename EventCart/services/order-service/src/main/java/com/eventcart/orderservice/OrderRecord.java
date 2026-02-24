package com.eventcart.orderservice;

public class OrderRecord {
    private String orderId;
    private OrderStatus status;
    private CreateOrderRequest request;

    public OrderRecord() {
    }

    public OrderRecord(String orderId, OrderStatus status, CreateOrderRequest request) {
        this.orderId = orderId;
        this.status = status;
        this.request = request;
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
}
