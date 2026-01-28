package com.aegis.backend.infrastructure.web.public_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty @Valid List<OrderItemRequest> items
) {}
