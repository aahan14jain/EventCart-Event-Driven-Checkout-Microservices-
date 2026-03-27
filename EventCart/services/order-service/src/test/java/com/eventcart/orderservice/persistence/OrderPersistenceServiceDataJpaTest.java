package com.eventcart.orderservice.persistence;

import com.eventcart.orderservice.CreateOrderRequest;
import com.eventcart.orderservice.OrderItem;
import com.eventcart.orderservice.OrderRecord;
import com.eventcart.orderservice.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(OrderPersistenceService.class)
@ActiveProfiles("postgres")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderdb-jpa;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.kafka.listener.auto-startup=false"
})
class OrderPersistenceServiceDataJpaTest {

    @Autowired
    private OrderPersistenceService orderPersistenceService;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Test
    void saveFindAndUpdateStatus_roundTripsThroughJpa() {
        var items = java.util.List.of(new OrderItem("SKU-1", 2), new OrderItem("SKU-2", 3));
        var request = new CreateOrderRequest(items, 42.5);
        request.setForcePaymentFailure(true);
        OrderRecord newOrder = new OrderRecord("order-100", OrderStatus.PENDING, request, "corr-100");

        OrderRecord saved = orderPersistenceService.saveNewOrder(newOrder);
        Optional<OrderRecord> loaded = orderPersistenceService.findById("order-100");
        Optional<OrderRecord> updated = orderPersistenceService.updateOrderStatus("order-100", OrderStatus.CONFIRMED);

        assertEquals("order-100", saved.getOrderId());
        assertNotNull(saved.getRequest());
        assertEquals(42.5, saved.getRequest().getTotalAmount());
        assertEquals(2, saved.getRequest().getItems().size());
        assertEquals("SKU-1", saved.getRequest().getItems().get(0).getSku());
        assertEquals(2, saved.getRequest().getItems().get(0).getQuantity());
        assertEquals(true, saved.getRequest().getForcePaymentFailure());

        assertTrue(loaded.isPresent());
        assertEquals(OrderStatus.PENDING, loaded.get().getStatus());

        assertNotNull(loaded.get().getRequest());
        assertEquals(42.5, loaded.get().getRequest().getTotalAmount());
        assertEquals(true, loaded.get().getRequest().getForcePaymentFailure());

        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.CONFIRMED, updated.get().getStatus());
        assertNotNull(updated.get().getRequest());
        assertEquals(42.5, updated.get().getRequest().getTotalAmount());
        assertEquals(true, updated.get().getRequest().getForcePaymentFailure());
        assertEquals(2, updated.get().getRequest().getItems().size());
        assertEquals("SKU-1", updated.get().getRequest().getItems().get(0).getSku());
        assertEquals(2, updated.get().getRequest().getItems().get(0).getQuantity());
    }

    @Test
    void save_setsCreatedAtAndUpdatedAt() {
        var items = java.util.List.of(new OrderItem("SKU-99", 9));
        var request = new CreateOrderRequest(items, 9.99);
        orderPersistenceService.saveNewOrder(new OrderRecord("order-101", OrderStatus.PENDING, request, "corr-101"));

        OrderEntity persisted = orderJpaRepository.findById("order-101").orElseThrow();
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        assertEquals("corr-101", persisted.getCorrelationId());
        assertEquals(9.99, persisted.getTotalAmount());
        assertEquals(1, persisted.getItems().size());
        assertEquals("SKU-99", persisted.getItems().get(0).getSku());
        assertEquals(9, persisted.getItems().get(0).getQuantity());
    }
}
