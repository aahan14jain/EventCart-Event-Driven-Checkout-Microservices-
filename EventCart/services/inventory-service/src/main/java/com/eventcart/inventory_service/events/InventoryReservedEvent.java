package com.eventcart.inventory_service.events;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryReservedEvent {

    private String orderId;
    /** From {@code order.created} when client requested demo payment failure (optional). */
    private Boolean forcePaymentFailure;

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

    public Boolean getForcePaymentFailure() {
        return forcePaymentFailure;
    }

    public void setForcePaymentFailure(Boolean forcePaymentFailure) {
        this.forcePaymentFailure = forcePaymentFailure;
    }
}
