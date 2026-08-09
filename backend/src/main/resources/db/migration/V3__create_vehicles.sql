CREATE TABLE vehicles (
    id           UUID            NOT NULL,
    plate        VARCHAR(16)     NOT NULL,
    type         VARCHAR(16)     NOT NULL,
    status       VARCHAR(16)     NOT NULL,
    created_at   TIMESTAMPTZ     NOT NULL,
    updated_at   TIMESTAMPTZ     NOT NULL,
    version      BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_vehicles PRIMARY KEY (id),
    CONSTRAINT uq_vehicles_plate UNIQUE (plate)
);

CREATE INDEX idx_vehicles_status ON vehicles (status);
CREATE INDEX idx_vehicles_type ON vehicles (type);
CREATE INDEX idx_vehicles_created_at ON vehicles (created_at);
