package com.aegis.backend.infrastructure.persistence.jpa;

import com.aegis.backend.shared.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLog, UUID> {
}
