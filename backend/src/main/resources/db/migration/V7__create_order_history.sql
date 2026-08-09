-- Append-only order timeline. No UPDATE/DELETE of historical rows by design.

CREATE TABLE order_history (
    id                UUID            NOT NULL,
    order_id          UUID            NOT NULL,
    event_type        VARCHAR(64)     NOT NULL,
    previous_status   VARCHAR(32),
    new_status        VARCHAR(32)     NOT NULL,
    description       VARCHAR(500),
    performed_by      UUID,
    created_at        TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_order_history PRIMARY KEY (id),
    CONSTRAINT fk_order_history_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX idx_order_history_order_created_at
    ON order_history (order_id, created_at);
