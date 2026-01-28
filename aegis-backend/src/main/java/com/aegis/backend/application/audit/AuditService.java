package com.aegis.backend.application.audit;

import com.aegis.backend.shared.audit.AuditLog;
import com.aegis.backend.shared.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.MANDATORY) // Só roda dentro de transação existente
    public void log(String entityId, String entityType, String action, String oldStatus, String newStatus, String payload) {
        AuditLog log = new AuditLog(
                entityId,
                entityType,
                action,
                oldStatus,
                newStatus,
                payload,
                "system" // Em um sistema real, pegaríamos do SecurityContext
        );
        auditLogRepository.save(log);
    }
}
