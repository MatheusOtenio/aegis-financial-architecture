package com.aegis.backend.infrastructure.persistence.repositories;

import com.aegis.backend.infrastructure.persistence.jpa.AuditLogJpaRepository;
import com.aegis.backend.shared.audit.AuditLog;
import com.aegis.backend.shared.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;

    @Override
    public void save(AuditLog log) {
        jpaRepository.save(log);
    }
}
