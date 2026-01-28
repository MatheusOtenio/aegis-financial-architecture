package com.aegis.backend.domain.report;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyReportRepository {
    DailyReport save(DailyReport report);
    Optional<DailyReport> findByDate(LocalDate date);
}
