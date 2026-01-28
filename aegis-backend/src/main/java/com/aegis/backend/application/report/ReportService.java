package com.aegis.backend.application.report;

import com.aegis.backend.application.audit.AuditService;
import com.aegis.backend.domain.report.DailyReport;
import com.aegis.backend.infrastructure.persistence.DailyReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final DailyReportRepository repository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public ReportService(DailyReportRepository repository, AuditService auditService, ObjectMapper objectMapper) {
        this.repository = repository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public record DailyReportDTO(
            LocalDate reportDate,
            Integer totalOrders,
            BigDecimal totalRevenue,
            List<TopItemDTO> topItems
    ) {}

    public record TopItemDTO(String name, Integer quantity) {}

    @Transactional
    public void processDailyReport(DailyReportDTO dto, String originService) {
        if (repository.findByReportDate(dto.reportDate()).isPresent()) {
            throw new IllegalArgumentException("Relatório já existe para a data: " + dto.reportDate());
        }

        String topItemsJson;
        try {
            topItemsJson = objectMapper.writeValueAsString(dto.topItems());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao processar JSON", e);
        }

        DailyReport report = new DailyReport(
                UUID.randomUUID(),
                dto.reportDate(),
                dto.totalOrders(),
                dto.totalRevenue(),
                topItemsJson,
                "PROCESSED"
        );

        repository.save(report);

        // Payload JSON válido para coluna json/jsonb
        String auditPayload = String.format(
                "{\"action\":\"REPORT_CREATED\",\"origin\":\"%s\"}",
                originService
        );

        auditService.log(
                "DailyReport",
                report.getReportDate().toString(),
                "CREATE",
                auditPayload,
                originService
        );
    }
}
