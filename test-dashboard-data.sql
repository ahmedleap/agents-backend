-- Dashboard fixture data.
-- Run this entire script in pgAdmin against the primary agents_of_leap database.
-- It only INSERTs rows. Every run creates a fresh, isolated fixture using UUIDs.

DO $$
DECLARE
    fixture_suffix text := lower(substr(replace(gen_random_uuid()::text, '-', ''), 1, 12));
    fixture_email text := 'dashboard-fixture.' || fixture_suffix || '@example.test';
    client_uuid uuid := gen_random_uuid();
    active_account_one uuid := gen_random_uuid();
    active_account_two uuid := gen_random_uuid();
    closed_account uuid := gen_random_uuid();
    instrument_one uuid := gen_random_uuid();
    instrument_two uuid := gen_random_uuid();
    instrument_three uuid := gen_random_uuid();
    snapshot_day date;
BEGIN
    INSERT INTO clients (
        client_id, first_name, last_name, email, password_hash, date_of_birth,
        join_date, email_verified, auth_status, failed_login_attempts
    ) VALUES (
        client_uuid, 'Dashboard', 'Fixture', fixture_email, 'dashboard-fixture-password',
        DATE '1990-01-15', CURRENT_TIMESTAMP, true, 'ACTIVE', 0
    );

    INSERT INTO accounts (
        account_id, client_id, name, cash_balance, status, open_date
    ) VALUES
        (active_account_one, client_uuid, 'Dashboard Active Brokerage', 10000.00, 'ACTIVE', CURRENT_TIMESTAMP),
        (active_account_two, client_uuid, 'Dashboard Active Retirement', 25000.00, 'ACTIVE', CURRENT_TIMESTAMP),
        (closed_account, client_uuid, 'Dashboard Closed Account', 5000.00, 'CLOSED', CURRENT_TIMESTAMP);

    INSERT INTO instruments (
        instrument_id, ticker, name, asset_class, industry, bid, ask, price_updated_at
    ) VALUES
        (instrument_one, 'D' || substr(fixture_suffix, 1, 8), 'Dashboard Fixture Growth Co', 'STOCK', 'Technology', 119.90, 120.10, CURRENT_TIMESTAMP),
        (instrument_two, 'E' || substr(fixture_suffix, 1, 8), 'Dashboard Fixture Value ETF', 'ETF', 'Diversified', 79.90, 80.10, CURRENT_TIMESTAMP),
        (instrument_three, 'F' || substr(fixture_suffix, 1, 8), 'Dashboard Fixture Healthcare', 'STOCK', 'Healthcare', 44.90, 45.10, CURRENT_TIMESTAMP);

    INSERT INTO watchlists (watchlist_id, client_id, instrument_id, added_at) VALUES
        (gen_random_uuid(), client_uuid, instrument_one, CURRENT_TIMESTAMP - INTERVAL '3 days'),
        (gen_random_uuid(), client_uuid, instrument_two, CURRENT_TIMESTAMP - INTERVAL '2 days');

    -- Two bars per instrument are required by the dashboard market-mover query.
    INSERT INTO instrument_price_history (
        price_history_id, instrument_id, timestamp, open, high, low, close, volume
    ) VALUES
        (gen_random_uuid(), instrument_one, CURRENT_TIMESTAMP - INTERVAL '2 days', 110.00, 112.00, 109.00, 110.00, 120000),
        (gen_random_uuid(), instrument_one, CURRENT_TIMESTAMP - INTERVAL '1 day', 118.00, 121.00, 117.00, 120.00, 180000),
        (gen_random_uuid(), instrument_two, CURRENT_TIMESTAMP - INTERVAL '2 days', 78.00, 79.00, 77.00, 78.00, 90000),
        (gen_random_uuid(), instrument_two, CURRENT_TIMESTAMP - INTERVAL '1 day', 80.00, 81.00, 79.00, 80.00, 130000),
        (gen_random_uuid(), instrument_three, CURRENT_TIMESTAMP - INTERVAL '2 days', 45.00, 46.00, 44.00, 45.00, 70000),
        (gen_random_uuid(), instrument_three, CURRENT_TIMESTAMP - INTERVAL '1 day', 44.00, 45.00, 43.00, 44.00, 110000);

    INSERT INTO holdings (
        holding_id, account_id, instrument_id, quantity, average_cost_basis
    ) VALUES
        (gen_random_uuid(), active_account_one, instrument_one, 20.000000, 100.0000),
        (gen_random_uuid(), active_account_two, instrument_two, 50.000000, 75.0000);

    -- Six rows verify the endpoint cap of five and exercise every current order status.
    INSERT INTO orders (
        order_id, account_id, instrument_id, order_type, quantity, limit_price,
        filled_price, status, created_at, filled_at, cancelled_at, cancel_reason
    ) VALUES
        (gen_random_uuid(), active_account_one, instrument_one, 'BUY', 2.000000, 118.0000, NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '6 minutes', NULL, NULL, NULL),
        (gen_random_uuid(), active_account_two, instrument_two, 'SELL', 3.000000, 80.0000, 80.0000, 'FILLED', CURRENT_TIMESTAMP - INTERVAL '5 minutes', CURRENT_TIMESTAMP - INTERVAL '4 minutes', NULL, NULL),
        (gen_random_uuid(), active_account_one, instrument_three, 'BUY', 4.000000, 45.0000, NULL, 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '4 minutes', NULL, CURRENT_TIMESTAMP - INTERVAL '3 minutes', 'Dashboard fixture cancellation'),
        (gen_random_uuid(), active_account_two, instrument_one, 'BUY', 5.000000, 119.0000, NULL, 'REJECTED', CURRENT_TIMESTAMP - INTERVAL '3 minutes', NULL, NULL, 'Dashboard fixture rejection'),
        (gen_random_uuid(), active_account_one, instrument_two, 'SELL', 6.000000, 80.0000, 80.0000, 'FILLED', CURRENT_TIMESTAMP - INTERVAL '2 minutes', CURRENT_TIMESTAMP - INTERVAL '1 minute', NULL, NULL),
        (gen_random_uuid(), active_account_two, instrument_three, 'BUY', 7.000000, 44.0000, NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '1 minute', NULL, NULL, NULL);

    -- Forty-five daily snapshots give both 30D and 90D requests useful data.
    FOR snapshot_day IN
        SELECT CURRENT_DATE - offset_days
        FROM generate_series(1, 45) AS day_series(offset_days)
    LOOP
        INSERT INTO historical_snapshot (
            snapshot_id, account_id, snapshot_date, cash_balance, holdings_value, total_value
        ) VALUES
            (gen_random_uuid(), active_account_one, snapshot_day,
             10000.00, 2000.00 + ((45 - (CURRENT_DATE - snapshot_day)) * 10.00),
             12000.00 + ((45 - (CURRENT_DATE - snapshot_day)) * 10.00)),
            (gen_random_uuid(), active_account_two, snapshot_day,
             25000.00, 4000.00 + ((45 - (CURRENT_DATE - snapshot_day)) * 15.00),
             29000.00 + ((45 - (CURRENT_DATE - snapshot_day)) * 15.00));
    END LOOP;

    RAISE NOTICE 'Dashboard fixture created. client_id=%, email=%', client_uuid, fixture_email;
END $$;

-- Copy this client_id into test-dashboard.ps1 when DatabaseUrl is not supplied.
SELECT client_id, email
FROM clients
WHERE email LIKE 'dashboard-fixture.%@example.test'
ORDER BY join_date DESC
LIMIT 1;