package com.aegis.backend.domain.report;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private Long totalOrders;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRevenue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    // Construtor completo (modelo derivado, não tem transição de estado complexa)
    public DailyReport(LocalDate date, Long totalOrders, BigDecimal totalRevenue) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.status = ReportStatus.CLOSED; // Status fixo conforme regra
    }
}
