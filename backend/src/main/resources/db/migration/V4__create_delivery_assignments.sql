CREATE TABLE delivery_assignments (
    id                  UUID            NOT NULL,
    order_id            UUID            NOT NULL,
    driver_id           UUID            NOT NULL,
    status              VARCHAR(32)     NOT NULL,
    assigned_at         TIMESTAMPTZ     NOT NULL,
    accepted_at         TIMESTAMPTZ,
    rejected_at         TIMESTAMPTZ,
    picked_up_at        TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    rejection_reason    VARCHAR(255),
    assignment_score    NUMERIC(12, 4),
    created_at          TIMESTAMPTZ     NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_delivery_assignments PRIMARY KEY (id),
    CONSTRAINT fk_delivery_assignments_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_delivery_assignments_driver
        FOREIGN KEY (driver_id) REFERENCES drivers (id)
);

CREATE INDEX idx_delivery_assignments_order_id ON delivery_assignments (order_id);
CREATE INDEX idx_delivery_assignments_driver_id ON delivery_assignments (driver_id);
CREATE INDEX idx_delivery_assignments_status ON delivery_assignments (status);
CREATE INDEX idx_delivery_assignments_order_status ON delivery_assignments (order_id, status);

CREATE UNIQUE INDEX uq_delivery_assignments_one_active_per_order
    ON delivery_assignments (order_id)
    WHERE status IN ('PENDING', 'ACCEPTED');
