package com.aegis.backend.application.report;

import com.aegis.backend.domain.report.DailyReport;
import com.aegis.backend.domain.report.DailyReportRepository;
import com.aegis.backend.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RegisterDailyReportUseCase {

    private final DailyReportRepository reportRepository;

    @Transactional
    public DailyReport execute(LocalDate date, Long totalOrders, BigDecimal totalRevenue) {
        // Validação de idempotência
        if (reportRepository.findByDate(date).isPresent()) {
            throw new DomainException("Report already exists for date: " + date);
        }

        DailyReport report = new DailyReport(date, totalOrders, totalRevenue);
        return reportRepository.save(report);
    }
}
