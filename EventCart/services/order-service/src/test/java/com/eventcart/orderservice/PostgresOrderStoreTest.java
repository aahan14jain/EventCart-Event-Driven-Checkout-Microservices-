package com.eventcart.orderservice;

import com.eventcart.orderservice.persistence.OrderPersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgresOrderStoreTest {

    @Mock
    private OrderPersistenceService orderPersistenceService;

    @InjectMocks
    private PostgresOrderStore store;

    @Test
    void save_delegatesToPersistenceService() {
        OrderRecord record = new OrderRecord("order-1", OrderStatus.PENDING, null, "corr-1");
        when(orderPersistenceService.saveNewOrder(record)).thenReturn(record);

        OrderRecord saved = store.save(record);

        assertEquals("order-1", saved.getOrderId());
        verify(orderPersistenceService).saveNewOrder(record);
    }

    @Test
    void findById_delegatesToPersistenceService() {
        OrderRecord record = new OrderRecord("order-2", OrderStatus.RESERVED, null, "corr-2");
        when(orderPersistenceService.findById("order-2")).thenReturn(Optional.of(record));

        Optional<OrderRecord> loaded = store.findById("order-2");

        assertTrue(loaded.isPresent());
        assertEquals(OrderStatus.RESERVED, loaded.get().getStatus());
        verify(orderPersistenceService).findById("order-2");
    }

    @Test
    void updateStatus_delegatesToPersistenceService() {
        OrderRecord record = new OrderRecord("order-3", OrderStatus.CONFIRMED, null, "corr-3");
        when(orderPersistenceService.updateOrderStatus("order-3", OrderStatus.CONFIRMED))
                .thenReturn(Optional.of(record));

        Optional<OrderRecord> updated = store.updateStatus("order-3", OrderStatus.CONFIRMED);

        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.CONFIRMED, updated.get().getStatus());
        verify(orderPersistenceService).updateOrderStatus("order-3", OrderStatus.CONFIRMED);
    }
}
