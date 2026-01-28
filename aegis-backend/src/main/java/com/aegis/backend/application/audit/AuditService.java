package com.aegis.backend.application.audit;

import com.aegis.backend.domain.audit.AuditLog;
import com.aegis.backend.infrastructure.persistence.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    // REQUIRES_NEW garante que o log seja salvo mesmo se a transação principal falhar
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entity, String entityId, String action, String payload, String origin) {
        AuditLog log = new AuditLog(entity, entityId, action, payload, origin);
        repository.save(log);
    }
}
