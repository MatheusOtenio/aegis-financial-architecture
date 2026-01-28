package com.aegis.backend.infrastructure.persistence;

import com.aegis.backend.domain.report.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, UUID> {
    // Busca relatório por data exata (garante unicidade de negócio)
    Optional<DailyReport> findByReportDate(LocalDate date);
}
