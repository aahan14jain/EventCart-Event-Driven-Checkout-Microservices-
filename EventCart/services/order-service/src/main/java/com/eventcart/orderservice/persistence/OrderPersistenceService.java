package com.eventcart.orderservice.persistence;

import com.eventcart.orderservice.CreateOrderRequest;
import com.eventcart.orderservice.OrderItem;
import com.eventcart.orderservice.OrderRecord;
import com.eventcart.orderservice.OrderStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("postgres")
public class OrderPersistenceService {

    private final OrderJpaRepository orderJpaRepository;

    public OrderPersistenceService(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Transactional
    public OrderRecord saveNewOrder(OrderRecord record) {
        double totalAmount = 0.0;
        List<OrderItem> items = List.of();
        Boolean forcePaymentFailure = null;
        if (record.getRequest() != null) {
            totalAmount = record.getRequest().getTotalAmount();
            if (record.getRequest().getItems() != null) {
                items = record.getRequest().getItems();
            }
            forcePaymentFailure = record.getRequest().getForcePaymentFailure();
        }

        OrderEntity entity = new OrderEntity(
                record.getOrderId(),
                record.getStatus(),
                record.getCorrelationId(),
                totalAmount,
                forcePaymentFailure);

        for (OrderItem item : items) {
            entity.addItem(new OrderItemEntity(entity, item.getSku(), item.getQuantity()));
        }

        OrderEntity saved = orderJpaRepository.save(entity);
        return toRecord(saved);
    }

    @Transactional
    public Optional<OrderRecord> updateOrderStatus(String orderId, OrderStatus newStatus) {
        return orderJpaRepository.findById(orderId)
                .map(entity -> {
                    entity.setStatus(newStatus);
                    OrderEntity saved = orderJpaRepository.save(entity);
                    return toRecord(saved);
                });
    }

    @Transactional(readOnly = true)
    public Optional<OrderRecord> findById(String orderId) {
        return orderJpaRepository.findById(orderId).map(this::toRecord);
    }

    private OrderRecord toRecord(OrderEntity entity) {
        List<OrderItem> domainItems = entity.getItems().stream()
                .map(i -> new OrderItem(i.getSku(), i.getQuantity()))
                .collect(Collectors.toList());

        CreateOrderRequest request = new CreateOrderRequest(domainItems, entity.getTotalAmount());
        request.setForcePaymentFailure(entity.getForcePaymentFailure());
        return new OrderRecord(entity.getOrderId(), entity.getStatus(), request, entity.getCorrelationId());
    }
}
