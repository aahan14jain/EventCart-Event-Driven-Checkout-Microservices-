package com.eventcart.orderservice;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryOrderStoreTest {

    private final InMemoryOrderStore store = new InMemoryOrderStore();

    @Test
    void saveAndFindById_returnsStoredOrder() {
        OrderRecord record = new OrderRecord("order-1", OrderStatus.PENDING, null, "corr-1");

        store.save(record);

        Optional<OrderRecord> loaded = store.findById("order-1");
        assertTrue(loaded.isPresent());
        assertEquals("order-1", loaded.get().getOrderId());
        assertEquals(OrderStatus.PENDING, loaded.get().getStatus());
        assertEquals("corr-1", loaded.get().getCorrelationId());
    }

    @Test
    void updateStatus_updatesExistingOrderInMemory() {
        store.save(new OrderRecord("order-2", OrderStatus.PENDING, null, "corr-2"));

        Optional<OrderRecord> updated = store.updateStatus("order-2", OrderStatus.CONFIRMED);

        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.CONFIRMED, updated.get().getStatus());
        assertEquals(OrderStatus.CONFIRMED, store.findById("order-2").orElseThrow().getStatus());
    }

    @Test
    void updateStatus_returnsEmptyWhenOrderMissing() {
        Optional<OrderRecord> updated = store.updateStatus("missing", OrderStatus.FAILED);
        assertTrue(updated.isEmpty());
    }
}
