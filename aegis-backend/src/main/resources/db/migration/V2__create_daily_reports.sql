CREATE TABLE daily_reports (
    id UUID PRIMARY KEY,
    report_date DATE NOT NULL UNIQUE,
    total_orders INTEGER NOT NULL,
    total_revenue NUMERIC(19, 2) NOT NULL,
    top_items JSONB NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
