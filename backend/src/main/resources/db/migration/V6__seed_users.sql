-- Demo users for local/dev. Password for all accounts: Fleetbite1!
-- Hash generated with BCrypt (strength 10). Never store plaintext passwords.

INSERT INTO users (id, email, password_hash, full_name, role, status, created_at, updated_at, version)
VALUES
    (
        '11111111-1111-1111-1111-111111111111',
        'admin@fleetbite.local',
        '$2b$10$hUCdccbBsfmJhHT/C85WA.GJOIXMQtvbtzMV86RNiKJyjUBBiAgsu',
        'FleetBite Admin',
        'ADMIN',
        'ACTIVE',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        0
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'dispatcher@fleetbite.local',
        '$2b$10$hUCdccbBsfmJhHT/C85WA.GJOIXMQtvbtzMV86RNiKJyjUBBiAgsu',
        'FleetBite Dispatcher',
        'DISPATCHER',
        'ACTIVE',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        0
    ),
    (
        '33333333-3333-3333-3333-333333333333',
        'operator@fleetbite.local',
        '$2b$10$hUCdccbBsfmJhHT/C85WA.GJOIXMQtvbtzMV86RNiKJyjUBBiAgsu',
        'FleetBite Operator',
        'RESTAURANT_OPERATOR',
        'ACTIVE',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        0
    ),
    (
        '44444444-4444-4444-4444-444444444444',
        'driver@fleetbite.local',
        '$2b$10$hUCdccbBsfmJhHT/C85WA.GJOIXMQtvbtzMV86RNiKJyjUBBiAgsu',
        'FleetBite Driver',
        'DRIVER',
        'ACTIVE',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        TIMESTAMPTZ '2026-08-08 22:00:00-05',
        0
    );
