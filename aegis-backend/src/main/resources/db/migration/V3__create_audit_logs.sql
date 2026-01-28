CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY,
                            entity_id VARCHAR(255) NOT NULL,
                            entity_type VARCHAR(100) NOT NULL,
                            action VARCHAR(50) NOT NULL,
                            changed_at TIMESTAMP NOT NULL,
                            changed_by VARCHAR(100), -- Pode ser nulo se for sistema
                            old_status VARCHAR(50),
                            new_status VARCHAR(50),
                            payload JSONB -- Armazena detalhes extras flexíveis
);

CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
