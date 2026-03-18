package com.eventcart.inventory_service.events;

public class InventoryReleasedEvent {

    private String orderId;

    public InventoryReleasedEvent() {
    }

    public InventoryReleasedEvent(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
