package com.aegis.backend.application.order;

import com.aegis.backend.application.audit.AuditService;
import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderRepository;
import com.aegis.backend.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayOrderUseCase {

    private final OrderRepository orderRepository;
    private final AuditService auditService; // Injeção nova

    @Transactional
    public void execute(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Order not found: " + orderId));

        String oldStatus = order.getStatus().name();

        order.pay();
        orderRepository.save(order);

        // Auditoria
        auditService.log(
                order.getId().toString(),
                "ORDER",
                "PAY",
                oldStatus,
                order.getStatus().name(),
                null // Payload opcional
        );
    }
}
