package com.aegis.backend.infrastructure.persistence.jpa;

import com.aegis.backend.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderSummaryView {
    UUID getId();
    LocalDateTime getCreatedAt();
    OrderStatus getStatus();
    BigDecimal getTotalAmount();
}
