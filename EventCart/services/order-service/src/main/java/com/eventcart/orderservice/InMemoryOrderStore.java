package com.eventcart.orderservice;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Non-postgres mode store: keeps existing in-memory behavior unchanged.
 */
@Component
@Profile("!postgres")
public class InMemoryOrderStore implements OrderStore {

    private final ConcurrentHashMap<String, OrderRecord> storage = new ConcurrentHashMap<>();

    @Override
    public OrderRecord save(OrderRecord record) {
        storage.put(record.getOrderId(), record);
        return record;
    }

    @Override
    public Optional<OrderRecord> updateStatus(String orderId, OrderStatus newStatus) {
        OrderRecord existing = storage.get(orderId);
        if (existing == null) {
            return Optional.empty();
        }
        existing.setStatus(newStatus);
        storage.put(orderId, existing);
        return Optional.of(existing);
    }

    @Override
    public Optional<OrderRecord> findById(String orderId) {
        return Optional.ofNullable(storage.get(orderId));
    }
}
