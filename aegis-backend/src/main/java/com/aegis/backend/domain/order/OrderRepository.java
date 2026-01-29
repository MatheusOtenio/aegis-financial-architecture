package com.aegis.backend.domain.order;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByDate(LocalDate date); // Será útil na API pública
    DailyOrderSlice findDailySummaries(LocalDate date, Collection<OrderStatus> statuses, int page, int size);
}
