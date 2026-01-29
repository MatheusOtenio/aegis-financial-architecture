package com.aegis.backend.infrastructure.persistence.repositories;

import com.aegis.backend.domain.order.Order;
import com.aegis.backend.domain.order.DailyOrderSlice;
import com.aegis.backend.domain.order.DailyOrderSummary;
import com.aegis.backend.domain.order.OrderRepository;
import com.aegis.backend.domain.order.OrderStatus;
import com.aegis.backend.infrastructure.persistence.jpa.OrderJpaRepository;
import com.aegis.backend.infrastructure.persistence.jpa.OrderSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
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
        return jpaRepository.findAllByCreatedAtBetween(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
    }

    @Override
    public DailyOrderSlice findDailySummaries(LocalDate date, Collection<OrderStatus> statuses, int page, int size) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime start = date.atStartOfDay(zoneId).toLocalDateTime();
        LocalDateTime end = date.plusDays(1).atStartOfDay(zoneId).toLocalDateTime();

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<OrderSummaryView> result = jpaRepository.findSummaryByCreatedAtBetweenAndStatusIn(start, end, statuses, pageable);

        List<DailyOrderSummary> items = result.getContent().stream()
                .map(v -> new DailyOrderSummary(v.getId(), v.getCreatedAt(), v.getStatus(), v.getTotalAmount()))
                .toList();

        return new DailyOrderSlice(items, result.hasNext());
    }
}
