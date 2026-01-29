package com.aegis.backend.infrastructure.persistence.jpa;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.OrderStatus;
import com.aegis.backend.infrastructure.persistence.jpa.OrderSummaryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    // Query customizada para buscar por intervalo de data (ignora hora)
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
    List<Order> findAllByCreatedAtBetween(@Param("startOfDay") LocalDateTime startOfDay,
                                          @Param("endOfDay") LocalDateTime endOfDay);

    Page<Order> findByCreatedAtBetweenAndStatusIn(LocalDateTime startOfDay,
                                                  LocalDateTime endOfDay,
                                                  Collection<OrderStatus> statuses,
                                                  Pageable pageable);

    Page<OrderSummaryView> findSummaryByCreatedAtBetweenAndStatusIn(LocalDateTime startOfDay,
                                                                    LocalDateTime endOfDay,
                                                                    Collection<OrderStatus> statuses,
                                                                    Pageable pageable);
}
