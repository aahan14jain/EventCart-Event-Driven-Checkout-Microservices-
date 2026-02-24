package com.eventcart.orderservice;

import java.util.List;

public class CreateOrderRequest {
    private List<OrderItem> items;
    private double totalAmount;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(List<OrderItem> items, double totalAmount) {
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
