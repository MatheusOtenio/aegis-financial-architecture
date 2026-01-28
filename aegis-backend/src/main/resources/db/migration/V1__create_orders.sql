CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        created_at TIMESTAMP NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        total_amount DECIMAL(19, 2) NOT NULL
);

CREATE TABLE order_items (
                             id UUID PRIMARY KEY,
                             order_id UUID NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             unit_price DECIMAL(19, 2) NOT NULL,
                             quantity INTEGER NOT NULL,
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Índices para performance
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_name ON order_items(name);
