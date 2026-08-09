CREATE TABLE orders (
    id                      UUID            NOT NULL,
    code                    VARCHAR(40)     NOT NULL,
    customer_name           VARCHAR(120)    NOT NULL,
    customer_phone          VARCHAR(32)     NOT NULL,
    delivery_address        VARCHAR(255)    NOT NULL,
    delivery_latitude       NUMERIC(10, 7)  NOT NULL,
    delivery_longitude      NUMERIC(10, 7)  NOT NULL,
    total_amount            NUMERIC(12, 2)  NOT NULL,
    status                  VARCHAR(32)     NOT NULL,
    priority                VARCHAR(16)     NOT NULL,
    promised_delivery_at    TIMESTAMPTZ     NOT NULL,
    created_at              TIMESTAMPTZ     NOT NULL,
    confirmed_at            TIMESTAMPTZ,
    preparation_started_at  TIMESTAMPTZ,
    ready_at                TIMESTAMPTZ,
    assigned_at             TIMESTAMPTZ,
    picked_up_at            TIMESTAMPTZ,
    in_transit_at           TIMESTAMPTZ,
    delivered_at            TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    failed_delivery_at      TIMESTAMPTZ,
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uq_orders_code UNIQUE (code),
    CONSTRAINT ck_orders_total_amount_non_negative CHECK (total_amount >= 0)
);

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_priority ON orders (priority);
CREATE INDEX idx_orders_promised_delivery_at ON orders (promised_delivery_at);
CREATE INDEX idx_orders_created_at ON orders (created_at);
CREATE INDEX idx_orders_operational_queue ON orders (status, priority, promised_delivery_at);
