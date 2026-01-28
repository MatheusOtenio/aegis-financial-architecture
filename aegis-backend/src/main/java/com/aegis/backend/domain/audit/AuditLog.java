package com.aegis.backend.domain.audit;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String origin;
    
    public AuditLog() {}
    
    public AuditLog(String entity, String entityId, String action, String payload, String origin) {
        this.id = UUID.randomUUID();
        this.entity = entity;
        this.entityId = entityId;
        this.action = action;
        this.payload = payload;
        this.origin = origin;
        this.createdAt = LocalDateTime.now();
    }
}
