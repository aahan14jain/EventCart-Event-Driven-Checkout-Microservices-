package com.eventcart.notification_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @GetMapping("/hello")
    public String hello() {
        return "Notification Service is running 🚀";
    }
}
