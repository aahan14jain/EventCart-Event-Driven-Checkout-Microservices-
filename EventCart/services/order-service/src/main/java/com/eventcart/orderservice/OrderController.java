package com.eventcart.orderservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderStore orderStore = new OrderStore();

    @GetMapping("/hello")
    public String hello() {
        return "Order Service is running 🚀";
    }

    @PostMapping("/")
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        OrderRecord record = new OrderRecord(orderId, OrderStatus.PENDING, request);
        orderStore.save(record);
        
        return new CreateOrderResponse(orderId, record.getStatus().name());
    }
}
