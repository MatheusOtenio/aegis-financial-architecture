package com.aegis.backend.shared.audit;

public interface AuditLogRepository {
    void save(AuditLog log);
}
