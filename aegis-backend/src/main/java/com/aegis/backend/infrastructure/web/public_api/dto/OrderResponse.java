package com.aegis.backend.infrastructure.web.public_api.dto;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderItem;
import com.aegis.backend.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        LocalDateTime createdAt,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses
        );
    }
}

record OrderItemResponse(String name, BigDecimal unitPrice, Integer quantity, BigDecimal total) {
    static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getName(), item.getUnitPrice(), item.getQuantity(), item.getTotal());
    }
}
