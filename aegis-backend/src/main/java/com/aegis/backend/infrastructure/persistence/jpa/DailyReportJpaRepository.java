package com.aegis.backend.infrastructure.persistence.jpa;

import com.aegis.backend.domain.report.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyReportJpaRepository extends JpaRepository<DailyReport, UUID> {
    Optional<DailyReport> findByDate(LocalDate date);
}
