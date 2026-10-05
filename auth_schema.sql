-- pgAdmin: connect to the target database, open Query Tool, load this file, and execute it.
-- Existing database only; for a fresh database run schema.sql, which already includes auth.
-- All statements are rerunnable and kept in one transaction.
BEGIN;

ALTER TABLE clients ADD COLUMN IF NOT EXISTS phone VARCHAR(32);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS country CHAR(2);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS auth_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_VERIFICATION';
ALTER TABLE clients ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS signup_ip INET;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS signup_device VARCHAR(512);
CREATE UNIQUE INDEX IF NOT EXISTS uq_clients_phone ON clients (phone) WHERE phone IS NOT NULL;

CREATE TABLE IF NOT EXISTS auth_sessions (
    session_id UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES clients (client_id) ON DELETE CASCADE,
    access_token_hash CHAR(64) NOT NULL UNIQUE,
    refresh_token_hash CHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    access_expires_at TIMESTAMP NOT NULL,
    refresh_expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    device VARCHAR(512),
    location VARCHAR(128)
);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_client ON auth_sessions (client_id, created_at DESC);

CREATE TABLE IF NOT EXISTS auth_one_time_tokens (
    token_id UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES clients (client_id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    token_type VARCHAR(24) NOT NULL CHECK (token_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_auth_tokens_client_type ON auth_one_time_tokens (client_id, token_type, expires_at);

COMMIT;
