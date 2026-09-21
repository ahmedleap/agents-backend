# Accounts API - Quick Reference Guide

## Endpoint Summary Table

| Method | Endpoint | Purpose | Returns |
|--------|----------|---------|---------|
| **GET** | `/accounts` | List all client accounts | `AccountListItem[]` |
| **POST** | `/accounts` | Create new account | `Account` (201) |
| **GET** | `/accounts/{id}` | Get account details | `AccountDetail` |
| **GET** | `/accounts/{id}/summary` | Get portfolio summary | `Summary` |
| **GET** | `/accounts/{id}/performance?period=1Y` | Get performance metrics | `Performance` |
| **GET** | `/accounts/client/{clientId}` | List client's accounts | `Account[]` |
| **PUT** | `/accounts/{id}` | Update account | `Account` |
| **POST** | `/accounts/{id}/deposit` | Deposit cash | `Transaction` |
| **POST** | `/accounts/{id}/withdraw` | Withdraw cash | `Transaction` |

---

## Request/Response Examples

### List Accounts (GET /accounts)
```bash
curl -X GET http://localhost:8080/api/accounts
```

### Create Account (POST /accounts)
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "My Investment Account",
    "initialCashBalance": 50000
  }'
```

### Get Account Detail (GET /accounts/{accountId})
```bash
curl -X GET http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000
```

### Get Portfolio Summary (GET /accounts/{accountId}/summary)
```bash
curl -X GET http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000/summary
```

### Get Performance (GET /accounts/{accountId}/performance?period=1Y)
```bash
curl -X GET "http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000/performance?period=1Y"
```

Supported periods: `1D`, `1W`, `1M`, `3M`, `6M`, `1Y`, `ALL`

### Get Client's Accounts (GET /accounts/client/{clientId})
```bash
curl -X GET http://localhost:8080/api/accounts/client/550e8400-e29b-41d4-a716-446655440000
```

### Update Account (PUT /accounts/{accountId})
```bash
curl -X PUT http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Account Name",
    "status": "ACTIVE"
  }'
```

### Deposit Cash (POST /accounts/{accountId}/deposit)
```bash
curl -X POST http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000}'
```

### Withdraw Cash (POST /accounts/{accountId}/withdraw)
```bash
curl -X POST http://localhost:8080/api/accounts/550e8400-e29b-41d4-a716-446655440000/withdraw \
  -H "Content-Type: application/json" \
  -d '{"amount": 2000}'
```

---

## Response DTO Structure

### AccountListItem (from GET /accounts)
```json
{
  "accountId": "UUID",
  "name": "string",
  "status": "ACTIVE|RESTRICTED|SUSPENDED|CLOSED",
  "cashBalance": "BigDecimal",
  "availableBalance": "BigDecimal",
  "totalValue": "BigDecimal"
}
```

### AccountDetail (from GET /accounts/{id})
```json
{
  "accountId": "UUID",
  "clientId": "UUID",
  "name": "string",
  "cashBalance": "BigDecimal",
  "availableBalance": "BigDecimal",
  "status": "ACTIVE|RESTRICTED|SUSPENDED|CLOSED",
  "openDate": "ISO8601",
  "holdingCount": "int",
  "orderCount": "int",
  "transactionCount": "int"
}
```

### Summary (from GET /accounts/{id}/summary)
```json
{
  "cashBalance": "BigDecimal",
  "availableBalance": "BigDecimal",
  "holdingsValue": "BigDecimal",
  "totalValue": "BigDecimal",
  "summaryDate": "ISO8601"
}
```

### Performance (from GET /accounts/{id}/performance?period=1Y)
```json
{
  "period": "1Y",
  "startingValue": "BigDecimal",
  "endingValue": "BigDecimal",
  "totalReturn": "BigDecimal",
  "returnPercentage": "BigDecimal",
  "realizedGainLoss": "BigDecimal",
  "unrealizedGainLoss": "BigDecimal",
  "periodStartDate": "ISO8601",
  "periodEndDate": "ISO8601"
}
```

### Transaction (from POST deposit/withdraw)
```json
{
  "message": "string",
  "newCashBalance": "BigDecimal",
  "transactionType": "DEPOSIT|WITHDRAWAL"
}
```

---

## Account Status State Machine

```
ACTIVE ──→ RESTRICTED ──┐
  ↓         ↓           │
  ├─→ SUSPENDED ────────┤
  │         │           │
  └─────────└──→ CLOSED ←┘

Rules:
• ACTIVE: Can transition to any status
• RESTRICTED: Can transition to ACTIVE, SUSPENDED, CLOSED
• SUSPENDED: Can only transition to CLOSED
• CLOSED: Terminal state (no transitions out)
```

---

## Field Descriptions

### Balance Fields
- **cashBalance**: Exact cash in account
- **availableBalance**: Cash minus reserved funds for pending orders
- **totalValue**: Cash + holdings market value
- **holdingsValue**: Sum of (quantity × current price) for all holdings

### Account Fields
- **accountId**: Unique identifier (UUID)
- **clientId**: Associated client
- **name**: User-friendly name
- **status**: Current account state
- **openDate**: Account creation date

### Performance Fields
- **period**: Performance period requested (1D, 1W, 1M, 3M, 6M, 1Y, ALL)
- **startingValue**: Portfolio value at period start
- **endingValue**: Current portfolio value
- **totalReturn**: Ending - Starting (absolute)
- **returnPercentage**: (Total Return / Starting) × 100
- **realizedGainLoss**: Gains from sold positions
- **unrealizedGainLoss**: Gains on open positions

---

## Common Workflows

### Step 1: Create Account
```
POST /api/accounts
→ Account (201)
→ Use returned accountId
```

### Step 2: Deposit Initial Funds
```
POST /api/accounts/{accountId}/deposit
→ Transaction (200)
→ Verify newCashBalance
```

### Step 3: Monitor Portfolio
```
GET /api/accounts/{accountId}/summary
→ Summary (200)
→ Check totalValue
```

### Step 4: Analyze Performance
```
GET /api/accounts/{accountId}/performance?period=1Y
→ Performance (200)
→ Review returnPercentage
```

---

## Error Codes

| Code | Error | When |
|------|-------|------|
| 200 | OK | Successful GET/PUT/POST (non-creation) |
| 201 | Created | Successful POST with new resource |
| 400 | Bad Request | Validation error or business logic error |
| 404 | Not Found | Account/Client not found |
| 422 | Unprocessable | Invalid status transition |
| 500 | Server Error | Unexpected error |

---

## Testing Tips

### Using Postman/Bruno
1. Create environment variable for accountId
2. Use tests to extract and store response IDs
3. Chain requests using {{accountId}} in paths

### Using cURL
```bash
# Save response to file
curl -X GET http://localhost:8080/api/accounts -o response.json

# Extract accountId with jq
ACCOUNT_ID=$(jq -r '.[0].accountId' response.json)

# Use in next request
curl -X GET http://localhost:8080/api/accounts/$ACCOUNT_ID
```

### Using Postman Tests
```javascript
// Extract and save accountId
const response = pm.response.json();
pm.environment.set("accountId", response.accountId);

// Verify response structure
pm.test("Response has all fields", function () {
    pm.expect(response).to.have.property('accountId');
    pm.expect(response).to.have.property('cashBalance');
});
```

---

## Database Schema References

### Accounts Table
```sql
CREATE TABLE accounts (
    account_id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    cash_balance NUMERIC(18,2) NOT NULL DEFAULT 0,
    status account_status NOT NULL DEFAULT 'ACTIVE',
    open_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(client_id)
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    txn_type transaction_type NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
```

---

## Performance Notes

- GET /accounts: O(n) - returns all accounts
- GET /accounts/{id}: O(1) - indexed by account_id
- GET /accounts/{id}/summary: O(n) - scans holdings for values
- POST deposit/withdraw: O(1) - simple update
- List by client: O(m) where m = client's accounts

For large portfolios (1000+ holdings), consider:
- Caching portfolio values
- Background job to update historical snapshots
- Read replicas for performance queries

---

## Configuration

### Environment Variables (add to application.properties)
```properties
# Account API Settings
accounts.max-cash-balance=999999999.99
accounts.enable-restrictions=true
accounts.calculate-performance-daily=false

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/agents_db
spring.datasource.username=postgres
spring.datasource.password=password
```

---

## Troubleshooting

### "Client not found" Error
- Verify client exists in database
- Check clientId is valid UUID format
- Run: `SELECT * FROM clients WHERE client_id = 'your-id';`

### "Account not found" Error
- Check accountId is valid UUID format
- Verify account exists in database
- Run: `SELECT * FROM accounts WHERE account_id = 'your-id';`

### "Insufficient funds" on Withdrawal
- Check availableBalance in summary
- Verify pending orders aren't reserving funds
- Run: `SELECT * FROM orders WHERE account_id = 'your-id' AND status = 'PENDING';`

### Performance Slow
- Check if instrument_prices table is indexed
- Verify database statistics are up to date
- Monitor slow query log

---

## Future Enhancements

- [ ] Pagination support (limit/offset)
- [ ] Filtering by status
- [ ] Sorting options
- [ ] Batch operations
- [ ] Real-time WebSocket updates
- [ ] Account reconciliation
- [ ] Multi-currency support
- [ ] Tax reporting
- [ ] Risk analysis
- [ ] Rebalancing suggestions
