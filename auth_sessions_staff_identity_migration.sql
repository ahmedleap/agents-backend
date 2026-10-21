-- Staff session compatibility migration for shared middleware/backend auth_sessions.
--
-- Why:
-- 1) Middleware stores sessions for both client and staff identities in auth_sessions.client_id.
-- 2) A foreign key to clients(client_id) rejects staff admin_id values.
-- 3) Middleware revocation sets access_token_hash = NULL, so the column must allow NULL.
--
-- Safe to run multiple times.

-- Drop any foreign key on auth_sessions.client_id that points to clients(client_id).
DO $$
DECLARE
    fk_name text;
BEGIN
    SELECT tc.constraint_name
    INTO fk_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_name = kcu.constraint_name
     AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage ccu
      ON tc.constraint_name = ccu.constraint_name
     AND tc.table_schema = ccu.table_schema
    WHERE tc.constraint_type = 'FOREIGN KEY'
      AND tc.table_schema = 'public'
      AND tc.table_name = 'auth_sessions'
      AND kcu.column_name = 'client_id'
      AND ccu.table_name = 'clients'
      AND ccu.column_name = 'client_id'
    LIMIT 1;

    IF fk_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE public.auth_sessions DROP CONSTRAINT %I', fk_name);
    END IF;
END $$;

-- Keep revoked sessions representable.
ALTER TABLE public.auth_sessions
    ALTER COLUMN access_token_hash DROP NOT NULL;

-- Keep the unique behavior for non-null access-token hashes.
DROP INDEX IF EXISTS uq_auth_sessions_access_token_hash;
CREATE UNIQUE INDEX IF NOT EXISTS uq_auth_sessions_access_token_hash
    ON public.auth_sessions (access_token_hash)
    WHERE access_token_hash IS NOT NULL;
