package com.eventcart.orderservice.events;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderCreatedEvent extends AbstractDomainEvent {
    private String orderId;
    private String status;
    private double totalAmount;
    /** Propagated to {@code inventory.reserved} for payment-service (demo only). */
    private Boolean forcePaymentFailure;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(String orderId, String status, double totalAmount) {
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Boolean getForcePaymentFailure() {
        return forcePaymentFailure;
    }

    public void setForcePaymentFailure(Boolean forcePaymentFailure) {
        this.forcePaymentFailure = forcePaymentFailure;
    }
}
