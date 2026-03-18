package com.eventcart.orderservice;

import com.eventcart.orderservice.events.OrderCreatedEvent;
import com.eventcart.orderservice.messaging.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderStore orderStore;
    private final OrderEventPublisher orderEventPublisher;

    public OrderController(OrderStore orderStore, OrderEventPublisher orderEventPublisher) {
        this.orderStore = orderStore;
        this.orderEventPublisher = orderEventPublisher;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Order Service is running 🚀";
    }

    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        OrderRecord record = new OrderRecord(orderId, OrderStatus.PENDING, request);
        orderStore.save(record);
        log.info("Order created: {}, status=PENDING", orderId);

        double totalAmount = record.getRequest() != null ? record.getRequest().getTotalAmount() : 0.0;
        OrderCreatedEvent event = new OrderCreatedEvent(
            orderId,
            record.getStatus().name(),
            totalAmount
        );
        orderEventPublisher.publishOrderCreated(event);
        log.info("Published order.created: orderId={}", orderId);

        return new CreateOrderResponse(orderId, record.getStatus().name());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<CreateOrderResponse> getOrder(@PathVariable String orderId) {
        return orderStore.findById(orderId)
                .map(record -> {
                    String status = record.getStatus().name();
                    log.info("Fetched order: {}, status={}", orderId, status);
                    return ResponseEntity.ok(new CreateOrderResponse(record.getOrderId(), status));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
