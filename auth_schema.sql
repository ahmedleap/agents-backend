-- Additive auth schema migration for existing databases.
-- Safe to run multiple times.

ALTER TABLE IF EXISTS public.auth_sessions
    ADD COLUMN IF NOT EXISTS access_token_hash CHAR(64);

ALTER TABLE IF EXISTS public.auth_sessions
    ADD COLUMN IF NOT EXISTS access_expires_at TIMESTAMP;

ALTER TABLE IF EXISTS public.auth_sessions
    ADD COLUMN IF NOT EXISTS device VARCHAR(512);

CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_sessions_access_token_hash
    ON public.auth_sessions (access_token_hash)
    WHERE access_token_hash IS NOT NULL;
