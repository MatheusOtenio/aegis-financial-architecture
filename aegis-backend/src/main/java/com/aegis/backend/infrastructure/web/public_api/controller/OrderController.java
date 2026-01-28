package com.aegis.backend.infrastructure.web.public_api.controller;

import com.aegis.backend.application.order.CancelOrderUseCase;
import com.aegis.backend.application.order.CreateOrderUseCase;
import com.aegis.backend.application.order.ListOrdersUseCase;
import com.aegis.backend.application.order.PayOrderUseCase;
import com.aegis.backend.domain.order.OrderItem;
import com.aegis.backend.infrastructure.web.public_api.dto.CreateOrderRequest;
import com.aegis.backend.infrastructure.web.public_api.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid CreateOrderRequest request) {
        // Converte DTO -> Domínio
        List<OrderItem> domainItems = request.items().stream()
                .map(i -> new OrderItem(i.name(), i.unitPrice(), i.quantity()))
                .toList();

        return OrderResponse.from(createOrderUseCase.execute(domainItems));
    }

    @PostMapping("/{id}/pay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pay(@PathVariable UUID id) {
        payOrderUseCase.execute(id);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) {
        cancelOrderUseCase.execute(id);
    }

    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return listOrdersUseCase.execute(date).stream()
                .map(OrderResponse::from)
                .toList();
    }
}
