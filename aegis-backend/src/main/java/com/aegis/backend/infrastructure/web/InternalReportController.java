package com.aegis.backend.infrastructure.web;

import com.aegis.backend.application.report.ReportService;
import com.aegis.backend.application.report.ReportService.DailyReportDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reports")
public class InternalReportController {

    private final ReportService service;

    public InternalReportController(ReportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> receiveReport(@RequestBody DailyReportDTO dto) {
        // O Filtro já garantiu a segurança antes de chegar aqui
        service.processDailyReport(dto, "BATCH-SERVICE");
        return ResponseEntity.ok().build();
    }
}
