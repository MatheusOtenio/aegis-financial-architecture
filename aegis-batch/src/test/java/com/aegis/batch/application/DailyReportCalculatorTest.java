package com.aegis.batch.application;

import com.aegis.batch.shared.dto.DailyReportDto;
import com.aegis.batch.shared.dto.OrderDto;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

class DailyReportCalculatorTest {

    private final DailyReportCalculator calculator = new DailyReportCalculator();

    @Test
    void shouldCalculateCorrectlyMixedOrders() {
        LocalDate date = LocalDate.of(2026, 1, 28);

        Flux<OrderDto> orders = Flux.just(
                new OrderDto("1", LocalDateTime.now(), "PAID", new BigDecimal("100.00")),
                new OrderDto("2", LocalDateTime.now(), "CREATED", new BigDecimal("50.00")), // Ignorar revenue
                new OrderDto("3", LocalDateTime.now(), "PAID", new BigDecimal("20.50")),
                new OrderDto("4", LocalDateTime.now(), "CANCELED", new BigDecimal("1000.00")) // Ignorar revenue
        );

        StepVerifier.create(calculator.calculate(date, orders))
                .expectNextMatches(report ->
                        report.totalOrders() == 4 &&
                                report.totalRevenue().compareTo(new BigDecimal("120.50")) == 0 &&
                                report.date().equals(date)
                )
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroForEmptyStream() {
        LocalDate date = LocalDate.of(2026, 1, 28);

        StepVerifier.create(calculator.calculate(date, Flux.empty()))
                .expectNextMatches(report ->
                        report.totalOrders() == 0 &&
                                report.totalRevenue().compareTo(BigDecimal.ZERO) == 0
                )
                .verifyComplete();
    }
}
