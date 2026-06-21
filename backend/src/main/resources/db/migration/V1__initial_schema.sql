CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

CREATE TABLE vehicle_states (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    vehicle_locked BOOLEAN NOT NULL,
    ac_enabled BOOLEAN NOT NULL,
    air_purification_enabled BOOLEAN NOT NULL,
    cabin_temperature REAL NOT NULL CHECK (cabin_temperature BETWEEN 16 AND 30),
    fan_level INTEGER NOT NULL CHECK (fan_level BETWEEN 1 AND 5),
    driver_seat_heating BOOLEAN NOT NULL,
    passenger_seat_heating BOOLEAN NOT NULL,
    seat_ventilation BOOLEAN NOT NULL,
    auto_headlights BOOLEAN NOT NULL,
    welcome_light BOOLEAN NOT NULL,
    window_open_percent INTEGER NOT NULL CHECK (window_open_percent BETWEEN 0 AND 100),
    mirrors_folded BOOLEAN NOT NULL,
    trunk_open BOOLEAN NOT NULL,
    sunshade_open BOOLEAN NOT NULL,
    child_lock BOOLEAN NOT NULL,
    sentry_enabled BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE user_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    location_sharing_enabled BOOLEAN NOT NULL,
    cabin_camera_enabled BOOLEAN NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE media_assets (
    id UUID PRIMARY KEY,
    object_key VARCHAR(255) NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(64) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    sha256 CHAR(64) NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('ACTIVE', 'DELETE_FAILED')),
    uploaded_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE discovery_contents (
    id UUID PRIMARY KEY,
    category VARCHAR(32) NOT NULL,
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    body TEXT NOT NULL,
    media_id UUID REFERENCES media_assets(id),
    status VARCHAR(24) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED')),
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_discovery_contents_status_published ON discovery_contents(status, published_at DESC);

CREATE TABLE content_follows (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_id UUID NOT NULL REFERENCES discovery_contents(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, content_id)
);

CREATE TABLE vehicle_reservations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    paint VARCHAR(32) NOT NULL,
    wheel VARCHAR(32) NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('SUBMITTED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_vehicle_reservations_user ON vehicle_reservations(user_id, created_at DESC);

CREATE TABLE maintenance_bookings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    service VARCHAR(32) NOT NULL,
    booking_date DATE NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('SUBMITTED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_maintenance_bookings_user ON maintenance_bookings(user_id, created_at DESC);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL,
    activated_at TIMESTAMPTZ,
    deactivated_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, plan)
);
