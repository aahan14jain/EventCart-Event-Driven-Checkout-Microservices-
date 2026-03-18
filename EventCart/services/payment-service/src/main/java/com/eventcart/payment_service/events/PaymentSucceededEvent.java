package com.eventcart.payment_service.events;

public class PaymentSucceededEvent {

    private String orderId;

    public PaymentSucceededEvent() {
    }

    public PaymentSucceededEvent(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
