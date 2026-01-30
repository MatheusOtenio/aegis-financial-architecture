package com.aegis.batch.application;

import com.aegis.batch.shared.dto.DailyReportDto;
import com.aegis.batch.shared.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class DailyReportCalculator {

    /**
     * Processa o stream de pedidos e gera o relatório consolidado.
     * Lógica:
     * 1. Total Orders: Conta tudo.
     * 2. Revenue: Soma apenas status 'PAID'.
     */
    public Mono<DailyReportDto> calculate(LocalDate reportDate, Flux<OrderDto> orderStream) {
        // Acumuladores atômicos (thread-safe para lambda, embora reactor seja serializado no subscriber)
        // Usaremos reduce para imutabilidade funcional onde possível

        return orderStream
                .reduce(new Accumulator(), (acc, order) -> {
                    acc.totalOrders++;

                    if ("PAID".equalsIgnoreCase(order.status())) {
                        acc.totalRevenue = acc.totalRevenue.add(order.totalAmount());
                    }
                    return acc;
                })
                .map(acc -> new DailyReportDto(
                        reportDate,
                        acc.totalOrders,
                        acc.totalRevenue
                ))
                .defaultIfEmpty(new DailyReportDto(reportDate, 0L, BigDecimal.ZERO));
    }

    // Classe auxiliar interna para acumulação mutável eficiente dentro do reduce
    private static class Accumulator {
        long totalOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
    }
}
