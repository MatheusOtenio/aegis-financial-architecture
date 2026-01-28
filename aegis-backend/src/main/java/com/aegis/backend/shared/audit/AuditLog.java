package com.aegis.backend.shared.audit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String action;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private String changedBy; // Ex: "system", "user-123", "batch"

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON) // Hibernate 6 suporte nativo a JSON
    private String payload;

    public AuditLog(String entityId, String entityType, String action, String oldStatus, String newStatus, String payload, String changedBy) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.payload = payload;
        this.changedBy = changedBy != null ? changedBy : "system";
        this.changedAt = LocalDateTime.now();
    }
}
