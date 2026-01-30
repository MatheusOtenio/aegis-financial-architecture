package com.aegis.batch.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDto(
        String id,
        LocalDateTime createdAt,
        String status,
        BigDecimal totalAmount
) {}
