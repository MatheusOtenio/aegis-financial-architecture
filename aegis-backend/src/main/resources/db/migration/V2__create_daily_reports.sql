CREATE TABLE daily_reports (
                               id UUID PRIMARY KEY,
                               date DATE NOT NULL UNIQUE,
                               total_orders BIGINT NOT NULL,
                               total_revenue DECIMAL(19, 2) NOT NULL,
                               status VARCHAR(50) NOT NULL
);
