-- Link drivers to users and vehicles; remove duplicated driver name; evolve vehicle statuses.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM drivers) THEN
        RAISE EXCEPTION
            'V8 migration aborted: drivers table has rows without user_id. Reset the database (flyway clean / recreate) before applying User-Driver-Vehicle links.';
    END IF;
END $$;

-- Vehicle status: ACTIVE -> AVAILABLE; introduce IN_USE for assigned vehicles.
UPDATE vehicles SET status = 'AVAILABLE' WHERE status = 'ACTIVE';

ALTER TABLE drivers DROP COLUMN name;

ALTER TABLE drivers
    ADD COLUMN user_id UUID NOT NULL,
    ADD COLUMN vehicle_id UUID;

ALTER TABLE drivers
    ADD CONSTRAINT fk_drivers_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    ADD CONSTRAINT uq_drivers_user_id UNIQUE (user_id),
    ADD CONSTRAINT fk_drivers_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    ADD CONSTRAINT uq_drivers_vehicle_id UNIQUE (vehicle_id);

CREATE INDEX idx_drivers_user_id ON drivers (user_id);
CREATE INDEX idx_drivers_vehicle_id ON drivers (vehicle_id);
