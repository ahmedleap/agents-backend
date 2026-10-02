-- Upgrade auth token times for an existing database using UTC-aware timestamps.
-- Existing TIMESTAMP values are interpreted as UTC to preserve the instants generated
-- by the application, which creates token times with ZoneOffset.UTC.
-- Run once in pgAdmin Query Tool against the application's database.
BEGIN;

ALTER TABLE auth_one_time_tokens
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE
        USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN expires_at TYPE TIMESTAMP WITH TIME ZONE
        USING expires_at AT TIME ZONE 'UTC',
    ALTER COLUMN used_at TYPE TIMESTAMP WITH TIME ZONE
        USING used_at AT TIME ZONE 'UTC';

ALTER TABLE auth_one_time_tokens
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

COMMIT;
