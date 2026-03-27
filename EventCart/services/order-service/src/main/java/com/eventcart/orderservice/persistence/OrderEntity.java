package com.eventcart.orderservice.persistence;

import com.eventcart.orderservice.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * First persistence model for order-service.
 * Kept intentionally minimal and not yet wired into API flow.
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false, length = 64)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    // Demo-friendly: during incremental Phase 5 evolution there may already be existing rows
    // with NULL total_amount. Keep this column nullable for backward compatibility.
    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "force_payment_failure")
    private Boolean forcePaymentFailure;

    @OneToMany(mappedBy = "parentOrder", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity() {
    }

    public OrderEntity(String orderId, OrderStatus status, String correlationId, double totalAmount, Boolean forcePaymentFailure) {
        this.orderId = orderId;
        this.status = status;
        this.correlationId = correlationId;
        this.totalAmount = totalAmount;
        this.forcePaymentFailure = forcePaymentFailure;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public double getTotalAmount() {
        return totalAmount != null ? totalAmount : 0.0;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Boolean getForcePaymentFailure() {
        return forcePaymentFailure;
    }

    public void setForcePaymentFailure(Boolean forcePaymentFailure) {
        this.forcePaymentFailure = forcePaymentFailure;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEntity> items) {
        this.items = items;
    }

    public void addItem(OrderItemEntity item) {
        this.items.add(item);
    }
}
