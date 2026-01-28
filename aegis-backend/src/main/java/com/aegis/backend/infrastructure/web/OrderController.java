package com.aegis.backend.infrastructure.web;

import com.aegis.backend.application.order.OrderService;
import com.aegis.backend.application.order.OrderService.CreateOrderCmd;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> createOrder(@RequestBody @Valid CreateOrderCmd cmd) {
        UUID id = service.createOrder(cmd);
        return ResponseEntity.created(URI.create("/api/orders/" + id))
                .body(Map.of("orderId", id));
    }
}
