package com.aegis.backend.infrastructure.web.public_api.controller;

import com.aegis.backend.application.report.GetDailyReportUseCase;
import com.aegis.backend.domain.report.DailyReport;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final GetDailyReportUseCase getDailyReportUseCase;

    @GetMapping("/{date}")
    public DailyReport get(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return getDailyReportUseCase.execute(date);
    }
}
