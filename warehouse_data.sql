-- ============================================================
-- WAREHOUSE TEST DATA - PROPER LOADING VERSION
-- This file will load test data reliably with error reporting
-- ============================================================

-- Set timezone for consistency
SET timezone = 'UTC';

-- Clear existing data (IMPORTANT: do this first)
TRUNCATE TABLE fact_historical_snapshots CASCADE;
TRUNCATE TABLE fact_watchlists CASCADE;
TRUNCATE TABLE fact_price_history CASCADE;
TRUNCATE TABLE fact_transactions CASCADE;
TRUNCATE TABLE fact_audit_logs CASCADE;
TRUNCATE TABLE fact_orders CASCADE;
TRUNCATE TABLE dim_instruments CASCADE;
TRUNCATE TABLE dim_accounts CASCADE;
TRUNCATE TABLE dim_clients CASCADE;

-- ============================================================
-- DIM_CLIENTS: Test Clients (6 clients)
-- ============================================================
INSERT INTO dim_clients (
    client_id, first_name, middle_name, last_name, email, date_of_birth, 
    join_date, portfolio_size_range, risk_tolerance, extract_timestamp
) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Alice', 'Marie', 'Johnson', 'alice.johnson@example.com', 
 '1985-05-15'::DATE, NOW() - INTERVAL '300 days', 'LARGE_5M_PLUS', 'HIGH', NOW()),

('550e8400-e29b-41d4-a716-446655440002', 'Bob', 'James', 'Smith', 'bob.smith@example.com', 
 '1982-08-22'::DATE, NOW() - INTERVAL '280 days', 'LARGE_5M_PLUS', 'HIGH', NOW()),

('550e8400-e29b-41d4-a716-446655440003', 'Carol', 'Lynn', 'Williams', 'carol.williams@example.com', 
 '1990-03-10'::DATE, NOW() - INTERVAL '260 days', 'MEDIUM_1M_5M', 'MEDIUM', NOW()),

('550e8400-e29b-41d4-a716-446655440004', 'David', 'Robert', 'Brown', 'david.brown@example.com', 
 '1988-11-05'::DATE, NOW() - INTERVAL '240 days', 'MEDIUM_1M_5M', 'MEDIUM', NOW()),

('550e8400-e29b-41d4-a716-446655440005', 'Emma', 'Catherine', 'Davis', 'emma.davis@example.com', 
 '1995-07-18'::DATE, NOW() - INTERVAL '220 days', 'SMALL_UNDER_1M', 'LOW', NOW()),

('550e8400-e29b-41d4-a716-446655440006', 'Frank', 'Michael', 'Miller', 'frank.miller@example.com', 
 '1992-09-30'::DATE, NOW() - INTERVAL '200 days', 'SMALL_UNDER_1M', 'LOW', NOW());

SELECT 'Inserted ' || COUNT(*) || ' clients' FROM dim_clients;

-- ============================================================
-- DIM_ACCOUNTS: Test Accounts (7 accounts)
-- ============================================================
INSERT INTO dim_accounts (
    account_id, client_id, account_name, cash_balance, status, open_date, extract_timestamp
) VALUES
('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 
 'Alice Primary Trading', 2500000.00, 'ACTIVE', NOW() - INTERVAL '300 days', NOW()),

('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 
 'Alice Crypto Portfolio', 1500000.00, 'ACTIVE', NOW() - INTERVAL '290 days', NOW()),

('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', 
 'Bob Trading Account', 3200000.00, 'ACTIVE', NOW() - INTERVAL '280 days', NOW()),

('660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440003', 
 'Carol Investment Account', 2100000.00, 'ACTIVE', NOW() - INTERVAL '260 days', NOW()),

('660e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440004', 
 'David Growth Portfolio', 1850000.00, 'ACTIVE', NOW() - INTERVAL '240 days', NOW()),

('660e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440005', 
 'Emma Conservative Fund', 750000.00, 'ACTIVE', NOW() - INTERVAL '220 days', NOW()),

('660e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440006', 
 'Frank Index Fund', 450000.00, 'ACTIVE', NOW() - INTERVAL '200 days', NOW());

SELECT 'Inserted ' || COUNT(*) || ' accounts' FROM dim_accounts;

-- ============================================================
-- DIM_INSTRUMENTS: Test Securities (12 instruments)
-- ============================================================
INSERT INTO dim_instruments (
    instrument_id, ticker, instrument_name, asset_class, industry, 
    bid, ask, mid_price, price_updated_at, extract_timestamp
) VALUES
('770e8400-e29b-41d4-a716-446655440001', 'AAPL', 'Apple Inc.', 'STOCKS', 'Technology', 
 175.80, 175.95, 175.87, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440002', 'MSFT', 'Microsoft Corporation', 'STOCKS', 'Technology', 
 378.50, 378.75, 378.62, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440003', 'JPM', 'JPMorgan Chase & Co.', 'STOCKS', 'Finance', 
 192.30, 192.50, 192.40, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440004', 'TSLA', 'Tesla Inc.', 'STOCKS', 'Automotive', 
 285.25, 285.50, 285.37, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440005', 'AMZN', 'Amazon.com Inc.', 'STOCKS', 'Retail', 
 172.10, 172.35, 172.22, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440006', 'BTC', 'Bitcoin', 'CRYPTO', 'Digital Currency', 
 42500.00, 42600.00, 42550.00, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440007', 'ETH', 'Ethereum', 'CRYPTO', 'Digital Currency', 
 2250.50, 2260.50, 2255.50, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440008', 'DOGE', 'Dogecoin', 'CRYPTO', 'Digital Currency', 
 0.35, 0.40, 0.37, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440009', 'TLT', 'iShares 20+ Year Treasury Bond ETF', 'BONDS', 'Fixed Income', 
 94.00, 95.00, 94.50, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440010', 'AGG', 'iShares Core U.S. Aggregate Bond ETF', 'BONDS', 'Fixed Income', 
 102.00, 104.00, 103.00, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440011', 'BND', 'Vanguard Total Bond Market ETF', 'BONDS', 'Fixed Income', 
 103.00, 105.00, 104.00, NOW(), NOW()),

('770e8400-e29b-41d4-a716-446655440012', 'IVV', 'iShares Core S&P 500 ETF', 'STOCKS', 'Index', 
 460.00, 465.00, 462.50, NOW(), NOW());

SELECT 'Inserted ' || COUNT(*) || ' instruments' FROM dim_instruments;

-- ============================================================
-- FACT_ORDERS: Orders (45 total: 20 filled, 15 cancelled, 10 pending)
-- ============================================================

-- FILLED ORDERS (20 orders)
INSERT INTO fact_orders (
    order_id, account_id, instrument_id, order_type, quantity, limit_price, 
    filled_price, status, created_at, filled_at, cancelled_at, cancel_reason, 
    duration_seconds, extract_timestamp
) VALUES
('880e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 
 'BUY', 150.00, 176.00, 175.87, 'FILLED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days' + INTERVAL '5 minutes', NULL, NULL, 300, NOW()),

('880e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 
 'BUY', 200.00, 379.00, 378.62, 'FILLED', NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days' + INTERVAL '3 minutes', NULL, NULL, 180, NOW()),

('880e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440003', 
 'SELL', 100.00, 192.00, 192.40, 'FILLED', NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days' + INTERVAL '2 minutes', NULL, NULL, 120, NOW()),

('880e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440004', 
 'BUY', 75.00, 286.00, 285.37, 'FILLED', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days' + INTERVAL '4 minutes', NULL, NULL, 240, NOW()),

('880e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440005', 
 'BUY', 300.00, 173.00, 172.22, 'FILLED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days' + INTERVAL '6 minutes', NULL, NULL, 360, NOW()),

('880e8400-e29b-41d4-a716-446655440006', '660e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440006', 
 'BUY', 0.5, 42800.00, 42550.00, 'FILLED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + INTERVAL '8 minutes', NULL, NULL, 480, NOW()),

('880e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440007', 
 'BUY', 5.0, 2300.00, 2255.50, 'FILLED', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days' + INTERVAL '10 minutes', NULL, NULL, 600, NOW()),

('880e8400-e29b-41d4-a716-446655440008', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 
 'BUY', 500.00, 176.50, 175.87, 'FILLED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days' + INTERVAL '2 minutes', NULL, NULL, 120, NOW()),

('880e8400-e29b-41d4-a716-446655440009', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440002', 
 'SELL', 250.00, 378.00, 378.62, 'FILLED', NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days' + INTERVAL '1 minute', NULL, NULL, 60, NOW()),

('880e8400-e29b-41d4-a716-446655440010', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440003', 
 'BUY', 200.00, 193.00, 192.40, 'FILLED', NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days' + INTERVAL '3 minutes', NULL, NULL, 180, NOW()),

('880e8400-e29b-41d4-a716-446655440011', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440010', 
 'BUY', 500.00, 95.50, 95.30, 'FILLED', NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days' + INTERVAL '5 minutes', NULL, NULL, 300, NOW()),

('880e8400-e29b-41d4-a716-446655440012', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440011', 
 'BUY', 300.00, 105.00, 104.60, 'FILLED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days' + INTERVAL '4 minutes', NULL, NULL, 240, NOW()),

('880e8400-e29b-41d4-a716-446655440013', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', 
 'BUY', 100.00, 176.50, 175.87, 'FILLED', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + INTERVAL '2 minutes', NULL, NULL, 120, NOW()),

('880e8400-e29b-41d4-a716-446655440014', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440010', 
 'BUY', 250.00, 95.50, 95.30, 'FILLED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '3 minutes', NULL, NULL, 180, NOW()),

('880e8400-e29b-41d4-a716-446655440015', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440010', 
 'BUY', 200.00, 95.50, 95.30, 'FILLED', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '5 minutes', NULL, NULL, 300, NOW()),

('880e8400-e29b-41d4-a716-446655440016', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440011', 
 'BUY', 150.00, 105.00, 104.60, 'FILLED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '4 minutes', NULL, NULL, 240, NOW()),

('880e8400-e29b-41d4-a716-446655440017', '660e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440010', 
 'BUY', 1000.00, 95.50, 95.30, 'FILLED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '6 minutes', NULL, NULL, 360, NOW()),

('880e8400-e29b-41d4-a716-446655440018', '660e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440012', 
 'BUY', 500.00, 85.60, 85.40, 'FILLED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '5 minutes', NULL, NULL, 300, NOW()),

('880e8400-e29b-41d4-a716-446655440019', '660e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440006', 
 'SELL', 0.25, 42400.00, 42550.00, 'FILLED', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours' + INTERVAL '7 minutes', NULL, NULL, 420, NOW()),

('880e8400-e29b-41d4-a716-446655440020', '660e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440007', 
 'BUY', 2.0, 2280.00, 2255.50, 'FILLED', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '8 hours' + INTERVAL '9 minutes', NULL, NULL, 540, NOW());

-- CANCELLED ORDERS (15 orders)
INSERT INTO fact_orders (
    order_id, account_id, instrument_id, order_type, quantity, limit_price, 
    filled_price, status, created_at, filled_at, cancelled_at, cancel_reason, 
    duration_seconds, extract_timestamp
) VALUES
('880e8400-e29b-41d4-a716-446655440021', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440005', 
 'BUY', 200.00, 280.00, NULL, 'CANCELLED', NOW() - INTERVAL '17 days', NULL, 
 NOW() - INTERVAL '17 days' + INTERVAL '30 minutes', 'Price movement', 1800, NOW()),

('880e8400-e29b-41d4-a716-446655440022', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 
 'SELL', 100.00, 390.00, NULL, 'CANCELLED', NOW() - INTERVAL '15 days', NULL, 
 NOW() - INTERVAL '15 days' + INTERVAL '45 minutes', 'User cancelled', 2700, NOW()),

('880e8400-e29b-41d4-a716-446655440023', '660e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440005', 
 'BUY', 150.00, 170.00, NULL, 'CANCELLED', NOW() - INTERVAL '12 days', NULL, 
 NOW() - INTERVAL '12 days' + INTERVAL '20 minutes', 'Insufficient funds', 1200, NOW()),

('880e8400-e29b-41d4-a716-446655440024', '660e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001', 
 'BUY', 75.00, 174.00, NULL, 'CANCELLED', NOW() - INTERVAL '10 days', NULL, 
 NOW() - INTERVAL '10 days' + INTERVAL '15 minutes', 'Market condition', 900, NOW()),

('880e8400-e29b-41d4-a716-446655440025', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440008', 
 'BUY', 50.0, 100.00, NULL, 'CANCELLED', NOW() - INTERVAL '8 days', NULL, 
 NOW() - INTERVAL '8 days' + INTERVAL '25 minutes', 'User cancelled', 1500, NOW()),

('880e8400-e29b-41d4-a716-446655440026', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440009', 
 'SELL', 100.00, 290.00, NULL, 'CANCELLED', NOW() - INTERVAL '6 days', NULL, 
 NOW() - INTERVAL '6 days' + INTERVAL '10 minutes', 'Price movement', 600, NOW()),

('880e8400-e29b-41d4-a716-446655440027', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440002', 
 'BUY', 50.00, 375.00, NULL, 'CANCELLED', NOW() - INTERVAL '4 days', NULL, 
 NOW() - INTERVAL '4 days' + INTERVAL '35 minutes', 'User cancelled', 2100, NOW()),

('880e8400-e29b-41d4-a716-446655440028', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440003', 
 'BUY', 50.00, 190.00, NULL, 'CANCELLED', NOW() - INTERVAL '2 days', NULL, 
 NOW() - INTERVAL '2 days' + INTERVAL '12 minutes', 'Insufficient funds', 720, NOW()),

('880e8400-e29b-41d4-a716-446655440029', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', 
 'BUY', 100.00, 174.00, NULL, 'CANCELLED', NOW() - INTERVAL '1 day', NULL, 
 NOW() - INTERVAL '1 day' + INTERVAL '40 minutes', 'Market condition', 2400, NOW()),

('880e8400-e29b-41d4-a716-446655440030', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440003', 
 'BUY', 50.00, 188.00, NULL, 'CANCELLED', NOW() - INTERVAL '18 hours', NULL, 
 NOW() - INTERVAL '18 hours' + INTERVAL '22 minutes', 'User cancelled', 1320, NOW()),

('880e8400-e29b-41d4-a716-446655440031', '660e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440006', 
 'BUY', 0.5, 43000.00, NULL, 'CANCELLED', NOW() - INTERVAL '16 hours', NULL, 
 NOW() - INTERVAL '16 hours' + INTERVAL '18 minutes', 'Price movement', 1080, NOW()),

('880e8400-e29b-41d4-a716-446655440032', '660e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440008', 
 'BUY', 100.0, 0.40, NULL, 'CANCELLED', NOW() - INTERVAL '14 hours', NULL, 
 NOW() - INTERVAL '14 hours' + INTERVAL '30 minutes', 'User cancelled', 1800, NOW()),

('880e8400-e29b-41d4-a716-446655440033', '660e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440007', 
 'SELL', 1.0, 2400.00, NULL, 'CANCELLED', NOW() - INTERVAL '12 hours', NULL, 
 NOW() - INTERVAL '12 hours' + INTERVAL '15 minutes', 'Insufficient holdings', 900, NOW()),

('880e8400-e29b-41d4-a716-446655440034', '660e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440008', 
 'BUY', 25.0, 100.00, NULL, 'CANCELLED', NOW() - INTERVAL '10 hours', NULL, 
 NOW() - INTERVAL '10 hours' + INTERVAL '25 minutes', 'Market condition', 1500, NOW()),

('880e8400-e29b-41d4-a716-446655440035', '660e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440009', 
 'BUY', 100.00, 86.00, NULL, 'CANCELLED', NOW() - INTERVAL '4 hours', NULL, 
 NOW() - INTERVAL '4 hours' + INTERVAL '20 minutes', 'User cancelled', 1200, NOW());

-- PENDING ORDERS (10 orders)
INSERT INTO fact_orders (
    order_id, account_id, instrument_id, order_type, quantity, limit_price, 
    filled_price, status, created_at, filled_at, cancelled_at, cancel_reason, 
    duration_seconds, extract_timestamp
) VALUES
('880e8400-e29b-41d4-a716-446655440036', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 
 'BUY', 200.00, 175.00, NULL, 'PENDING', NOW() - INTERVAL '2 hours', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440037', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440002', 
 'SELL', 150.00, 380.00, NULL, 'PENDING', NOW() - INTERVAL '1 hour 30 minutes', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440038', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440003', 
 'BUY', 100.00, 191.00, NULL, 'PENDING', NOW() - INTERVAL '1 hour', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440039', '660e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440004', 
 'BUY', 50.00, 284.00, NULL, 'PENDING', NOW() - INTERVAL '45 minutes', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440040', '660e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440005', 
 'BUY', 100.00, 171.00, NULL, 'PENDING', NOW() - INTERVAL '30 minutes', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440041', '660e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440006', 
 'BUY', 1.0, 42300.00, NULL, 'PENDING', NOW() - INTERVAL '20 minutes', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440042', '660e8400-e29b-41d4-a716-446655440007', '770e8400-e29b-41d4-a716-446655440010', 
 'BUY', 500.00, 94.00, NULL, 'PENDING', NOW() - INTERVAL '15 minutes', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440043', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 
 'BUY', 100.00, 377.00, NULL, 'PENDING', NOW() - INTERVAL '10 minutes', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440044', '660e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440011', 
 'BUY', 200.00, 103.00, NULL, 'PENDING', NOW() - INTERVAL '5 minutes', NULL, NULL, NULL, NULL, NOW()),

('880e8400-e29b-41d4-a716-446655440045', '660e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440009', 
 'BUY', 500.0, 0.37, NULL, 'PENDING', NOW() - INTERVAL '2 minutes', NULL, NULL, NULL, NULL, NOW());

SELECT 'Inserted ' || COUNT(*) || ' orders' FROM fact_orders;

-- ============================================================
-- FACT_HISTORICAL_SNAPSHOTS: Portfolio snapshots
-- ============================================================
INSERT INTO fact_historical_snapshots (
    snapshot_id, account_id, snapshot_date, cash_balance, holdings_value, total_value, extract_timestamp
) VALUES
('990e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', NOW()::DATE - INTERVAL '30 days', 1800000.00, 700000.00, 2500000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440001', NOW()::DATE - INTERVAL '20 days', 1650000.00, 850000.00, 2500000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', NOW()::DATE - INTERVAL '10 days', 1500000.00, 950000.00, 2450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440001', NOW()::DATE, 1550000.00, 1000000.00, 2550000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440002', NOW()::DATE - INTERVAL '30 days', 1200000.00, 300000.00, 1500000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440006', '660e8400-e29b-41d4-a716-446655440002', NOW()::DATE - INTERVAL '20 days', 1000000.00, 550000.00, 1550000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440002', NOW()::DATE - INTERVAL '10 days', 900000.00, 650000.00, 1550000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440008', '660e8400-e29b-41d4-a716-446655440002', NOW()::DATE, 950000.00, 700000.00, 1650000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440009', '660e8400-e29b-41d4-a716-446655440003', NOW()::DATE - INTERVAL '30 days', 2500000.00, 700000.00, 3200000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440010', '660e8400-e29b-41d4-a716-446655440003', NOW()::DATE - INTERVAL '20 days', 2200000.00, 1000000.00, 3200000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440011', '660e8400-e29b-41d4-a716-446655440003', NOW()::DATE - INTERVAL '10 days', 1900000.00, 1350000.00, 3250000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440012', '660e8400-e29b-41d4-a716-446655440003', NOW()::DATE, 1850000.00, 1450000.00, 3300000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440013', '660e8400-e29b-41d4-a716-446655440004', NOW()::DATE - INTERVAL '30 days', 1800000.00, 300000.00, 2100000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440014', '660e8400-e29b-41d4-a716-446655440004', NOW()::DATE - INTERVAL '20 days', 1600000.00, 500000.00, 2100000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440015', '660e8400-e29b-41d4-a716-446655440004', NOW()::DATE - INTERVAL '10 days', 1400000.00, 700000.00, 2100000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440016', '660e8400-e29b-41d4-a716-446655440004', NOW()::DATE, 1300000.00, 800000.00, 2100000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440017', '660e8400-e29b-41d4-a716-446655440005', NOW()::DATE - INTERVAL '30 days', 600000.00, 150000.00, 750000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440018', '660e8400-e29b-41d4-a716-446655440005', NOW()::DATE - INTERVAL '20 days', 550000.00, 200000.00, 750000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440019', '660e8400-e29b-41d4-a716-446655440005', NOW()::DATE - INTERVAL '10 days', 500000.00, 250000.00, 750000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440020', '660e8400-e29b-41d4-a716-446655440005', NOW()::DATE, 475000.00, 275000.00, 750000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440021', '660e8400-e29b-41d4-a716-446655440006', NOW()::DATE - INTERVAL '30 days', 350000.00, 100000.00, 450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440022', '660e8400-e29b-41d4-a716-446655440006', NOW()::DATE - INTERVAL '20 days', 330000.00, 120000.00, 450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440023', '660e8400-e29b-41d4-a716-446655440006', NOW()::DATE - INTERVAL '10 days', 310000.00, 140000.00, 450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440024', '660e8400-e29b-41d4-a716-446655440006', NOW()::DATE, 290000.00, 160000.00, 450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440025', '660e8400-e29b-41d4-a716-446655440007', NOW()::DATE - INTERVAL '30 days', 300000.00, 150000.00, 450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440026', '660e8400-e29b-41d4-a716-446655440007', NOW()::DATE - INTERVAL '20 days', 280000.00, 170000.00, 450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440027', '660e8400-e29b-41d4-a716-446655440007', NOW()::DATE - INTERVAL '10 days', 260000.00, 190000.00, 450000.00, NOW()),
('990e8400-e29b-41d4-a716-446655440028', '660e8400-e29b-41d4-a716-446655440007', NOW()::DATE, 240000.00, 210000.00, 450000.00, NOW());

SELECT 'Inserted ' || COUNT(*) || ' snapshots' FROM fact_historical_snapshots;

-- ============================================================
-- VERIFICATION: Check data was inserted
-- ============================================================
SELECT '=== DATA LOAD SUMMARY ===' as summary;
SELECT 'Clients: ' || COUNT(*) FROM dim_clients;
SELECT 'Accounts: ' || COUNT(*) FROM dim_accounts;
SELECT 'Instruments: ' || COUNT(*) FROM dim_instruments;
SELECT 'Orders: ' || COUNT(*) FROM fact_orders;
SELECT 'Snapshots: ' || COUNT(*) FROM fact_historical_snapshots;
SELECT 'Orders created_at >= NOW() - 30 days: ' || COUNT(*) FROM fact_orders WHERE created_at >= NOW() - INTERVAL '30 days';

-- Done
SELECT 'All data loaded successfully!' as result;
