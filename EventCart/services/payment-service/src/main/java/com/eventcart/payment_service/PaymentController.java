package com.eventcart.payment_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @GetMapping("/hello")
    public String hello() {
        return "Payment Service is running 🚀";
    }
}
