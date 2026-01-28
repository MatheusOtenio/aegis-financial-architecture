package com.aegis.backend.application.order;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderItem;
import com.aegis.backend.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    @Transactional
    public Order execute(List<OrderItem> items) {
        // Cria o agregado raiz (já valida regras de domínio no construtor)
        Order newOrder = new Order(items);
        return orderRepository.save(newOrder);
    }
}
