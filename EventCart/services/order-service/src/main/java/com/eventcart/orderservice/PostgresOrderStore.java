package com.eventcart.orderservice;

import com.eventcart.orderservice.persistence.OrderPersistenceService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Postgres mode store: database is the source of truth.
 */
@Component
@Profile("postgres")
public class PostgresOrderStore implements OrderStore {

    private final OrderPersistenceService orderPersistenceService;

    public PostgresOrderStore(OrderPersistenceService orderPersistenceService) {
        this.orderPersistenceService = orderPersistenceService;
    }

    @Override
    public OrderRecord save(OrderRecord record) {
        return orderPersistenceService.saveNewOrder(record);
    }

    @Override
    public Optional<OrderRecord> updateStatus(String orderId, OrderStatus newStatus) {
        return orderPersistenceService.updateOrderStatus(orderId, newStatus);
    }

    @Override
    public Optional<OrderRecord> findById(String orderId) {
        return orderPersistenceService.findById(orderId);
    }
}
