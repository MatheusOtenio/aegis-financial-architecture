package com.aegis.backend.application.order;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Order> execute(LocalDate date) {
        if (date == null) {
            // Regra defensiva: se não passar data, assume hoje
            date = LocalDate.now();
        }
        return orderRepository.findByDate(date);
    }
}
