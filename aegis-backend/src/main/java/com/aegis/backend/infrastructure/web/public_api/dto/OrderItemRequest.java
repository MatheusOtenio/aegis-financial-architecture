package com.aegis.backend.infrastructure.web.public_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank String name,
        @NotNull @Min(0) BigDecimal unitPrice,
        @NotNull @Min(1) Integer quantity
) {}
