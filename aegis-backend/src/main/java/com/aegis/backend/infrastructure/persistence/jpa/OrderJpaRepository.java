package com.aegis.backend.infrastructure.persistence.jpa;

import com.aegis.backend.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    // Query customizada para buscar por intervalo de data (ignora hora)
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
    List<Order> findAllByCreatedAtBetween(@Param("startOfDay") LocalDateTime startOfDay,
                                          @Param("endOfDay") LocalDateTime endOfDay);
}
