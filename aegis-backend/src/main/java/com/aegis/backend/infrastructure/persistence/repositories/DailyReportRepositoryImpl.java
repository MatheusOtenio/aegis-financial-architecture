package com.aegis.backend.infrastructure.persistence.repositories;

import com.aegis.backend.domain.report.DailyReport;
import com.aegis.backend.domain.report.DailyReportRepository;
import com.aegis.backend.infrastructure.persistence.jpa.DailyReportJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyReportRepositoryImpl implements DailyReportRepository {

    private final DailyReportJpaRepository jpaRepository;

    @Override
    public DailyReport save(DailyReport report) {
        return jpaRepository.save(report);
    }

    @Override
    public Optional<DailyReport> findByDate(LocalDate date) {
        return jpaRepository.findByDate(date);
    }
}
