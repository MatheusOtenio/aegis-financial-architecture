package com.aegis.batch.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyReportDto(
        LocalDate date,
        long totalOrders,
        BigDecimal totalRevenue
        // TopItems removido por enquanto pois o endpoint de orders simplificado
        // não traz itens, apenas totais. Focaremos nos agregados principais.
) {}
