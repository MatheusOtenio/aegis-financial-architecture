CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    entity VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    payload JSONB,
    created_at TIMESTAMP NOT NULL,
    origin VARCHAR(100) -- Para rastrear quem chamou (Batch/Internal)
);
