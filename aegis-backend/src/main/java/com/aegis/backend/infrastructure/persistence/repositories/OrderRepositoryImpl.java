package com.aegis.backend.infrastructure.persistence.repositories;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderRepository;
import com.aegis.backend.infrastructure.persistence.jpa.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Order> findByDate(LocalDate date) {
        // Converte LocalDate para range de LocalDateTime para consultar no banco
        return jpaRepository.findAllByCreatedAtBetween(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
    }
}
