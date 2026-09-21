# Accounts API - Complete Endpoint Documentation

## Base URL
```
http://localhost:8080/api/accounts
```

## Endpoints Overview

### 1. GET /accounts
**List all accounts for the authenticated client with summary information**

#### Request
```http
GET /api/accounts
Content-Type: application/json
```

#### Response (200 OK)
```json
[
  {
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Investment Portfolio - Tech Stocks",
    "status": "ACTIVE",
    "cashBalance": 50000.00,
    "availableBalance": 48000.00,
    "totalValue": 102000.00
  },
  {
    "accountId": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Retirement Account",
    "status": "ACTIVE",
    "cashBalance": 75000.00,
    "availableBalance": 75000.00,
    "totalValue": 150000.00
  }
]
```

#### Response Fields
- `accountId` (UUID): Unique account identifier
- `name` (string): User-friendly account name
- `status` (string): Account status (ACTIVE, RESTRICTED, SUSPENDED, CLOSED)
- `cashBalance` (BigDecimal): Current cash balance
- `availableBalance` (BigDecimal): Cash available for withdrawal (cash - reserved for pending orders)
- `totalValue` (BigDecimal): Total portfolio value (cash + holdings value)

---

### 2. POST /accounts
**Create a new account for a client**

#### Request
```http
POST /api/accounts
Content-Type: application/json

{
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Investment Portfolio - Tech Stocks",
  "initialCashBalance": 50000.00
}
```

#### Request Body Fields
- `clientId` (UUID, required): UUID of an existing client
- `name` (string, required): Account name (1-255 characters)
- `initialCashBalance` (BigDecimal, required): Starting cash balance (must be positive)

#### Response (201 CREATED)
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Investment Portfolio - Tech Stocks",
  "cashBalance": 50000.00,
  "status": "ACTIVE",
  "openDate": "2024-12-01T10:30:00"
}
```

#### Validation Rules
- Client must exist in database
- Account name cannot be blank
- Initial cash balance must be positive
- New accounts are initialized with ACTIVE status

---

### 3. GET /accounts/{accountId}
**Retrieve detailed account information with balance status and related counts**

#### Request
```http
GET /api/accounts/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
```

#### Path Parameters
- `accountId` (UUID, required): Account ID to retrieve

#### Response (200 OK)
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Investment Portfolio - Tech Stocks",
  "cashBalance": 50000.00,
  "availableBalance": 48000.00,
  "status": "ACTIVE",
  "openDate": "2024-12-01T10:30:00",
  "holdingCount": 5,
  "orderCount": 2,
  "transactionCount": 3
}
```

#### Response Fields
- All fields from account summary
- `holdingCount` (int): Number of active holdings
- `orderCount` (int): Number of orders (all statuses)
- `transactionCount` (int): Number of cash transactions

---

### 4. GET /accounts/{accountId}/summary
**Retrieve portfolio valuation summary with cash, holdings value, and total value**

#### Request
```http
GET /api/accounts/550e8400-e29b-41d4-a716-446655440000/summary
Content-Type: application/json
```

#### Path Parameters
- `accountId` (UUID, required): Account ID

#### Response (200 OK)
```json
{
  "cashBalance": 50000.00,
  "availableBalance": 48000.00,
  "holdingsValue": 52000.00,
  "totalValue": 102000.00,
  "summaryDate": "2024-12-21T15:45:30"
}
```

#### Response Fields
- `cashBalance` (BigDecimal): Current cash in account
- `availableBalance` (BigDecimal): Cash available for withdrawal (excluding reserved for pending orders)
- `holdingsValue` (BigDecimal): Current market value of all holdings (based on latest prices)
- `totalValue` (BigDecimal): Total portfolio value (cash + holdings)
- `summaryDate` (string): Timestamp when summary was calculated (ISO 8601)

#### Business Logic
- Cash balance is the exact balance in the account
- Available balance excludes funds reserved for pending BUY orders
- Holdings value is calculated using the latest available instrument prices
- If no price data exists, holdings are valued at cost basis

---

### 5. GET /accounts/{accountId}/performance
**Retrieve account performance metrics for a specified period**

#### Request
```http
GET /api/accounts/550e8400-e29b-41d4-a716-446655440000/performance?period=1Y
Content-Type: application/json
```

#### Path Parameters
- `accountId` (UUID, required): Account ID

#### Query Parameters
- `period` (string, optional): Performance period
  - Valid values: `1D`, `1W`, `1M`, `3M`, `6M`, `1Y`, `ALL`
  - Default: `1Y` (past 1 year)

#### Response (200 OK)
```json
{
  "period": "1Y",
  "startingValue": 90000.00,
  "endingValue": 102000.00,
  "totalReturn": 12000.00,
  "returnPercentage": 13.33,
  "realizedGainLoss": 5000.00,
  "unrealizedGainLoss": 2500.00,
  "periodStartDate": "2023-12-21T00:00:00",
  "periodEndDate": "2024-12-21T15:45:30"
}
```

#### Response Fields
- `period` (string): Requested performance period
- `startingValue` (BigDecimal): Portfolio value at start of period
- `endingValue` (BigDecimal): Portfolio value at end of period
- `totalReturn` (BigDecimal): Absolute return in currency ($)
- `returnPercentage` (BigDecimal): Return as percentage (%)
- `realizedGainLoss` (BigDecimal): Gains/losses from completed trades
- `unrealizedGainLoss` (BigDecimal): Current gains/losses on open positions
- `periodStartDate` (string): Start date of performance period (ISO 8601)
- `periodEndDate` (string): End date of performance period (ISO 8601)

#### Calculation Details
- **Starting Value**: Portfolio value on the first trading day of the period
- **Ending Value**: Current portfolio value
- **Total Return**: Ending Value - Starting Value
- **Return %**: (Total Return / Starting Value) × 100
- **Realized G/L**: Gains from sold positions during the period
- **Unrealized G/L**: Current market value of holdings minus cost basis

---

### 6. GET /accounts/client/{clientId}
**Retrieve all accounts belonging to a specific client**

#### Request
```http
GET /api/accounts/client/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
```

#### Path Parameters
- `clientId` (UUID, required): Client ID

#### Response (200 OK)
```json
[
  {
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "clientId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Investment Portfolio - Tech Stocks",
    "cashBalance": 50000.00,
    "status": "ACTIVE",
    "openDate": "2024-12-01T10:30:00"
  },
  {
    "accountId": "550e8400-e29b-41d4-a716-446655440001",
    "clientId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Retirement Account",
    "cashBalance": 75000.00,
    "status": "ACTIVE",
    "openDate": "2024-11-15T09:00:00"
  }
]
```

#### Response
- Returns list of Account objects
- Ordered by `openDate` DESC (newest first)
- Returns empty array if client has no accounts

---

### 7. PUT /accounts/{accountId}
**Update account name and/or status**

#### Request
```http
PUT /api/accounts/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json

{
  "name": "Updated Portfolio - Mixed Assets",
  "status": "RESTRICTED"
}
```

#### Path Parameters
- `accountId` (UUID, required): Account ID to update

#### Request Body Fields
- `name` (string, optional): New account name
- `status` (string, optional): New account status
  - Valid values: `ACTIVE`, `RESTRICTED`, `SUSPENDED`, `CLOSED`

#### Response (200 OK)
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Updated Portfolio - Mixed Assets",
  "cashBalance": 50000.00,
  "status": "RESTRICTED",
  "openDate": "2024-12-01T10:30:00"
}
```

#### Status Transition Rules
- **ACTIVE** → Can transition to RESTRICTED, SUSPENDED, or CLOSED
- **RESTRICTED** → Can transition to ACTIVE, SUSPENDED, or CLOSED
- **SUSPENDED** → Can only transition to CLOSED
- **CLOSED** → Cannot transition to any other status

#### Validation Rules
- Account name cannot be blank if provided
- Status transitions must follow the state machine rules
- Both fields are optional (at least one must be provided)

---

### 8. POST /accounts/{accountId}/deposit
**Deposit cash into an account**

#### Request
```http
POST /api/accounts/550e8400-e29b-41d4-a716-446655440000/deposit
Content-Type: application/json

{
  "amount": 5000.00
}
```

#### Path Parameters
- `accountId` (UUID, required): Account ID to deposit to

#### Request Body Fields
- `amount` (BigDecimal, required): Amount to deposit (must be positive)

#### Response (200 OK)
```json
{
  "message": "Deposit successful. Amount: 5000.00",
  "newCashBalance": 55000.00,
  "transactionType": "DEPOSIT"
}
```

#### Response Fields
- `message` (string): Transaction confirmation message
- `newCashBalance` (BigDecimal): Cash balance after deposit
- `transactionType` (string): Transaction type ("DEPOSIT")

#### Validation Rules
- Account must be in ACTIVE status
- Deposit amount must be positive
- Amount is immediately available in cash balance

#### Business Logic
- Creates a transaction record in the database
- Updates account cash balance instantly
- Funds are available for immediate withdrawal or investment

---

### 9. POST /accounts/{accountId}/withdraw
**Withdraw cash from an account**

#### Request
```http
POST /api/accounts/550e8400-e29b-41d4-a716-446655440000/withdraw
Content-Type: application/json

{
  "amount": 2000.00
}
```

#### Path Parameters
- `accountId` (UUID, required): Account ID to withdraw from

#### Request Body Fields
- `amount` (BigDecimal, required): Amount to withdraw (must be positive)

#### Response (200 OK)
```json
{
  "message": "Withdrawal successful. Amount: 2000.00",
  "newCashBalance": 48000.00,
  "transactionType": "WITHDRAWAL"
}
```

#### Response Fields
- `message` (string): Transaction confirmation message
- `newCashBalance` (BigDecimal): Cash balance after withdrawal
- `transactionType` (string): Transaction type ("WITHDRAWAL")

#### Validation Rules
- Account must be in ACTIVE status
- Withdrawal amount must be positive
- Withdrawal cannot exceed available balance
- Available balance excludes funds reserved for pending orders

#### Error Response (400 Bad Request)
```json
{
  "error": "Insufficient funds. Available: 48000.00, Requested: 100000.00"
}
```

#### Business Logic
- Creates a transaction record in the database
- Updates account cash balance instantly
- Reserved funds (from pending BUY orders) cannot be withdrawn
- Available balance = cash_balance - reserved_funds

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Client not found: 550e8400-e29b-41d4-a716-446655440000"
}
```

### 404 Not Found
```json
{
  "error": "Account not found: 550e8400-e29b-41d4-a716-446655440000"
}
```

### 422 Unprocessable Entity
```json
{
  "error": "Cannot transition from CLOSED status"
}
```

---

## Account Status Reference

### ACTIVE
- Fully operational account
- Can deposit, withdraw, trade
- Default status for new accounts

### RESTRICTED
- Limited operations
- Can view portfolio and history
- Cannot place new orders (admin override)
- Can deposit/withdraw

### SUSPENDED
- Severely restricted
- Can only view data
- Cannot perform transactions
- Used for compliance holds

### CLOSED
- No operations allowed
- Account is archived
- No further transactions possible
- Historical data retained

---

## Pagination & Filtering

*Current implementation returns all results. Future versions may implement:*

```http
GET /api/accounts?limit=10&offset=0&status=ACTIVE
GET /api/accounts/client/{clientId}?limit=20&sortBy=openDate&order=desc
```

---

## Rate Limiting

*Not currently implemented. Consider adding for production:*
- 100 requests per minute per account
- 1000 requests per minute per client
- 10000 requests per minute per IP

---

## Authentication & Authorization

*Currently not enforced. Should implement in production:*
- JWT token required in Authorization header
- Endpoints should enforce client ownership
- Admin endpoints separate with role-based access

---

## Example Workflows

### Create Account and Make Deposit
```bash
# 1. Create account
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "New Investment Account",
    "initialCashBalance": 10000.00
  }'

# Response includes accountId: "abc123..."

# 2. Deposit additional funds
curl -X POST http://localhost:8080/api/accounts/abc123.../deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000.00}'

# 3. Check summary
curl -X GET http://localhost:8080/api/accounts/abc123.../summary

# 4. Check 1-year performance
curl -X GET "http://localhost:8080/api/accounts/abc123.../performance?period=1Y"
```

### Monitor Account Status
```bash
# Get all accounts for a client
curl -X GET http://localhost:8080/api/accounts/client/550e8400-e29b-41d4-a716-446655440000

# Get detailed account info
curl -X GET http://localhost:8080/api/accounts/abc123...

# Restrict account temporarily
curl -X PUT http://localhost:8080/api/accounts/abc123... \
  -H "Content-Type: application/json" \
  -d '{"status": "RESTRICTED"}'
```

---

## Implementation Notes

### Database Schema
- All UUIDs stored as VARCHAR in PostgreSQL
- Cash balances stored as NUMERIC(18,2)
- Timestamps stored as TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- Account status stored as Enum type

### Performance Considerations
- Portfolio value calculated at query time (not cached)
- For large portfolios, consider caching strategy
- Historical snapshots used for performance calculations
- Consider database indexes on account_id, client_id, status

### Future Enhancements
- WebSocket support for real-time balance updates
- Bulk operations (multiple deposits/withdrawals)
- Account reconciliation tools
- Multi-currency support
- Scheduled transactions
- Tax reporting features
