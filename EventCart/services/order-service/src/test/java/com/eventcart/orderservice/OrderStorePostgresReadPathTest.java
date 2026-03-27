package com.eventcart.orderservice;

import com.eventcart.orderservice.cache.RedisOrderCacheService;
import com.eventcart.orderservice.messaging.OrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("postgres")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:orderdb-store;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.kafka.listener.auto-startup=false",
        "spring.main.allow-bean-definition-overriding=true"
})
@Import(OrderStorePostgresReadPathTest.MockConfig.class)
class OrderStorePostgresReadPathTest {

    @Autowired
    private OrderStore orderStore;

    @Test
    @Transactional
    void saveThenFindById_returnsPersistedOrderDetails() {
        String orderId = "order-200";
        String correlationId = "corr-200";

        var items = List.of(
                new OrderItem("SKU-A", 1),
                new OrderItem("SKU-B", 2)
        );
        var request = new CreateOrderRequest(items, 77.7);
        request.setForcePaymentFailure(null);

        OrderRecord toSave = new OrderRecord(orderId, OrderStatus.PENDING, request, correlationId);

        orderStore.save(toSave);

        var loaded = orderStore.findById(orderId);
        assertTrue(loaded.isPresent());

        OrderRecord record = loaded.orElseThrow();
        assertEquals(OrderStatus.PENDING, record.getStatus());

        assertNotNull(record.getRequest());
        assertEquals(77.7, record.getRequest().getTotalAmount());
        assertNotNull(record.getRequest().getItems());
        assertEquals(2, record.getRequest().getItems().size());
        assertEquals("SKU-A", record.getRequest().getItems().get(0).getSku());
        assertEquals(1, record.getRequest().getItems().get(0).getQuantity());
        assertEquals("SKU-B", record.getRequest().getItems().get(1).getSku());
        assertEquals(2, record.getRequest().getItems().get(1).getQuantity());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        OrderEventPublisher orderEventPublisher() {
            return Mockito.mock(OrderEventPublisher.class);
        }

        @Bean
        @Primary
        RedisOrderCacheService redisOrderCacheService() {
            return Mockito.mock(RedisOrderCacheService.class);
        }
    }
}

