CREATE TABLE drivers (
    id                   UUID            NOT NULL,
    name                 VARCHAR(120)    NOT NULL,
    phone                VARCHAR(32)     NOT NULL,
    status               VARCHAR(16)     NOT NULL,
    current_latitude     NUMERIC(10, 7),
    current_longitude    NUMERIC(10, 7),
    created_at           TIMESTAMPTZ     NOT NULL,
    updated_at           TIMESTAMPTZ     NOT NULL,
    version              BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_drivers PRIMARY KEY (id),
    CONSTRAINT uq_drivers_phone UNIQUE (phone),
    CONSTRAINT ck_drivers_location_pair CHECK (
        (current_latitude IS NULL AND current_longitude IS NULL)
        OR (current_latitude IS NOT NULL AND current_longitude IS NOT NULL)
    )
);

CREATE INDEX idx_drivers_status ON drivers (status);
CREATE INDEX idx_drivers_created_at ON drivers (created_at);
