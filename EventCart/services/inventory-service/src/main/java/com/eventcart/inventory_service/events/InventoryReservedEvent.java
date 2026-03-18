package com.eventcart.inventory_service.events;

public class InventoryReservedEvent {

    private String orderId;

    public InventoryReservedEvent() {
    }

    public InventoryReservedEvent(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
