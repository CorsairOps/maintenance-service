CREATE TABLE IF NOT EXISTS maintenance_orders (
    id SERIAL PRIMARY KEY,
    asset_id VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL CHECK(status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    priority INT NOT NULL CHECK(priority BETWEEN 1 AND 5),
    placed_by VARCHAR(255),
    completed_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_asset_id ON maintenance_orders(asset_id);
CREATE INDEX idx_status ON maintenance_orders(status);
CREATE INDEX idx_priority ON maintenance_orders(priority);