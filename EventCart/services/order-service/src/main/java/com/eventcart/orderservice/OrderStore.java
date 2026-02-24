package com.eventcart.orderservice;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OrderStore {
    private final ConcurrentHashMap<String, OrderRecord> storage = new ConcurrentHashMap<>();

    public OrderRecord save(OrderRecord record) {
        storage.put(record.getOrderId(), record);
        return record;
    }

    public Optional<OrderRecord> findById(String orderId) {
        return Optional.ofNullable(storage.get(orderId));
    }
}
