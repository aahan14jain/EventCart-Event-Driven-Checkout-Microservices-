package com.eventcart.inventory_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @GetMapping("/hello")
    public String hello() {
        return "Inventory Service is running 🚀";
    }
}
