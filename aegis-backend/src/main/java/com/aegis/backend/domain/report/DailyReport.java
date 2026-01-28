package com.aegis.backend.domain.report;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_reports")
public class DailyReport {

    @Id
    private UUID id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "top_items", nullable = false, columnDefinition = "jsonb")
    private String topItems;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public DailyReport() {}

    public DailyReport(UUID id, LocalDate reportDate, Integer totalOrders, BigDecimal totalRevenue, String topItems, String status) {
        this.id = id;
        this.reportDate = reportDate;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.topItems = topItems;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters mínimos para o JSON serialization funcionar se necessário
    public LocalDate getReportDate() { return reportDate; }
}
