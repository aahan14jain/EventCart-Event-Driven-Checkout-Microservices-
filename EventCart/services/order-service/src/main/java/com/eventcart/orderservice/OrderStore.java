package com.eventcart.orderservice;

import java.util.Optional;

/** Authoritative order state abstraction used by controller/listeners. */
public interface OrderStore {

    OrderRecord save(OrderRecord record);

    Optional<OrderRecord> updateStatus(String orderId, OrderStatus newStatus);

    Optional<OrderRecord> findById(String orderId);
}
