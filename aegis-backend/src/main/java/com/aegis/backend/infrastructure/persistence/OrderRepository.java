package com.aegis.backend.infrastructure.persistence;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Busca pedidos pendentes para processamento
    List<Order> findByStatus(OrderStatus status);
    
    // Para relatórios: busca pedidos em um intervalo de tempo
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
