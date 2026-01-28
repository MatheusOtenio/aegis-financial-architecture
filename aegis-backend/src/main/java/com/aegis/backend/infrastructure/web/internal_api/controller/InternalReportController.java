package com.aegis.backend.infrastructure.web.internal_api.controller;

import com.aegis.backend.application.report.RegisterDailyReportUseCase;
import com.aegis.backend.domain.report.DailyReport;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/internal/reports")
@RequiredArgsConstructor
public class InternalReportController {

    private final RegisterDailyReportUseCase registerDailyReportUseCase;

    @Value("${aegis.security.service-token}")
    private String expectedToken;

    @PostMapping("/daily")
    public ResponseEntity<DailyReport> registerDailyReport(
            @RequestHeader("X-SERVICE-TOKEN") String token,
            @RequestBody @Valid DailyReportRequest request) {

        // Validação de segurança manual (simples e eficaz para este escopo)
        if (!expectedToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Service Token");
        }

        DailyReport report = registerDailyReportUseCase.execute(
                request.date(),
                request.totalOrders(),
                request.totalRevenue()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }
}

// DTO interno (record local)
record DailyReportRequest(
        @NotNull LocalDate date,
        @NotNull Long totalOrders,
        @NotNull BigDecimal totalRevenue
) {}
