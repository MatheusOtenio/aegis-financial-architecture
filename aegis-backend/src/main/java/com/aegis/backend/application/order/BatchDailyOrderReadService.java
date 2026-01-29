package com.aegis.backend.application.order;

import com.aegis.backend.domain.order.DailyOrderSlice;
import com.aegis.backend.domain.order.OrderRepository;
import com.aegis.backend.domain.order.OrderStatus;
import com.aegis.backend.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BatchDailyOrderReadService {

    private static final int MAX_PAGE_SIZE = 1000;

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DailyOrderSlice read(LocalDate date, int page, int size) {
        if (date == null) {
            throw new DomainException("Invalid date");
        }
        if (page < 0 || size <= 0) {
            throw new DomainException("Invalid pagination parameters");
        }

        int cappedSize = Math.min(size, MAX_PAGE_SIZE);
        Set<OrderStatus> eligible = EnumSet.allOf(OrderStatus.class);

        return orderRepository.findDailySummaries(date, eligible, page, cappedSize);
    }
}
