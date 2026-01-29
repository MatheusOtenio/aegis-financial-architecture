package com.aegis.backend.domain.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DailyOrderSummary(
        UUID id,
        LocalDateTime createdAt,
        OrderStatus status,
        BigDecimal totalAmount
) {}
