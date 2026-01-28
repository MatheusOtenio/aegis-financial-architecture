-- 1. USERS (Base do sistema)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 2. AUDIT_LOGS (Baseado no seu AuditLog.java)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    entity VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    payload JSONB,                    -- Mapeado como @JdbcTypeCode(SqlTypes.JSON)
    origin VARCHAR(255),              -- Campo opcional no Java
    created_at TIMESTAMP NOT NULL
);

-- 3. DAILY_REPORTS (Baseado no seu DailyReport.java)
CREATE TABLE daily_reports (
    id UUID PRIMARY KEY,
    report_date DATE NOT NULL UNIQUE,      -- LocalDate + Unique
    total_orders INTEGER NOT NULL,
    total_revenue NUMERIC(38, 2) NOT NULL, -- BigDecimal
    top_items JSONB NOT NULL,              -- JSONB
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 4. ORDERS (Baseado no seu Order.java)
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    table_number INTEGER,                  -- Opcional (Integer wrapper)
    status VARCHAR(255) NOT NULL,          -- EnumType.STRING
    total_amount NUMERIC(38, 2) NOT NULL,  -- BigDecimal
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 5. ORDER_ITEMS (Baseado no seu OrderItem.java)
CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,                -- Chave estrangeira para orders
    name VARCHAR(255) NOT NULL,            -- Nome do produto (já que Product não existe)
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(38, 2) NOT NULL,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id)
);