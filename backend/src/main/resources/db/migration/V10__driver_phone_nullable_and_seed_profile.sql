-- Driver phone is completed after user creation via PUT /drivers/{id}.
ALTER TABLE drivers ALTER COLUMN phone DROP NOT NULL;

-- Provision operational profile for the seeded DRIVER user (no phone/vehicle yet).
INSERT INTO drivers (
    id, user_id, phone, status, current_latitude, current_longitude,
    vehicle_id, created_at, updated_at, version
) VALUES (
    '55555555-5555-5555-5555-555555555555',
    '44444444-4444-4444-4444-444444444444',
    NULL,
    'OFFLINE',
    NULL,
    NULL,
    NULL,
    TIMESTAMPTZ '2026-08-08 22:00:00-05',
    TIMESTAMPTZ '2026-08-08 22:00:00-05',
    0
);
