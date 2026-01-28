package com.aegis.backend.application.report;

import com.aegis.backend.domain.report.DailyReport;
import com.aegis.backend.domain.report.DailyReportRepository;
import com.aegis.backend.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GetDailyReportUseCase {

    private final DailyReportRepository reportRepository;

    @Transactional(readOnly = true)
    public DailyReport execute(LocalDate date) {
        return reportRepository.findByDate(date)
                .orElseThrow(() -> new DomainException("Report not found for date: " + date));
    }
}
