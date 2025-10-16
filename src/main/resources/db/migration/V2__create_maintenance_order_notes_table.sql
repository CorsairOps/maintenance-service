
CREATE TABLE IF NOT EXISTS maintenance_order_notes (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES maintenance_orders(id) ON DELETE CASCADE,
    note TEXT NOT NULl,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_id ON maintenance_order_notes(order_id);