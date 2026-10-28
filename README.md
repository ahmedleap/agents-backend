# Agents Backend - Spring Boot + MyBatis Trading Platform

A high-performance Java backend for a trading/investment platform using Spring Boot, MyBatis, and PostgreSQL.

## Technology Stack

- **Java 21** (LTS - Temurin/Adoptium)
- **Spring Boot 3.3.1** (Web, no JPA)
- **MyBatis 3.0.3** (SQL Mapper, annotation-based)
- **PostgreSQL 42.6.0** (Database driver)
- **Maven 3.9.9** (Build tool)

## Market Movers API

Authenticated `GET /api/instruments/market-movers` reads the shared PostgreSQL
snapshot published by ticker_service. The backend never calls Alpaca or
calculates price changes for this endpoint. It returns up to five database-tracked
stock/crypto movers in persisted rank order; forex is excluded. Provider filtering
can yield fewer than five items and is not an exhaustive ranking of tracked symbols.

Before deploying, manually execute ticker_service's
`migrations/003_add_market_movers.sql` in pgAdmin against the shared primary
database, then start the ticker producer and backend. The backend database role
needs SELECT access to `market_movers` and `instruments`. No schema creation
occurs in this backend. Do not rerun the full legacy schema for this feature.

Response example:

```json
{
  "marketMovers": [{"ticker": "BTC", "price": 65000.25, "changePercent": -2.5}],
  "asOf": "2026-10-22T14:00:00Z",
  "refreshedAt": "2026-10-22T14:00:05Z",
  "isStale": false
}
```

`changePercent` is signed percentage points, not a fractional return. `asOf` is
the oldest selected provider timestamp; `refreshedAt` is publication time. Either
timestamp older than 60 seconds marks the response stale. Empty/uninitialized
snapshots return 200 with an empty array, null timestamps, and `isStale: true`.
Database failures return 503 and anonymous requests return 401.

Ticker refresh runs every ten seconds only on weekdays 09:30-16:00 ET, including
for crypto. Outside that window the prior snapshot remains. The existing hours
guard does not handle holidays or early closes. Alpaca provides the price and
percentage using each market's own daily-reset semantics. The producer preserves
the last snapshot if a required provider request or write fails.

Basic PowerShell smoke checks on port 8081 (set your access token locally):

```powershell
curl.exe -i --max-time 10 http://localhost:8081/actuator/health
curl.exe -i --max-time 10 http://localhost:8081/api/instruments/market-movers
curl.exe -i --max-time 10 http://localhost:8081/api/instruments/market-movers -H "Authorization: Bearer $env:ACCESS_TOKEN"
```

The frontend should consume `marketMovers` from this endpoint. The legacy
dashboard query remains unchanged; frontend E2E testing is manual.

## Prerequisites

### System Requirements
- **OS**: Windows, macOS, or Linux
- **Java**: OpenJDK 21+ (Temurin recommended)
- **Maven**: 3.9.0+ 
- **PostgreSQL**: 12+ (local or remote)

### Verify Installation
```powershell
java -version
mvn -v
psql --version
```

Expected:
```
openjdk version "21.0.3" (Temurin)
Apache Maven 3.9.9
psql (PostgreSQL) 12.x
```

---

## Local Setup Instructions

### 1. Clone Repository
```bash
git clone <repository-url>
cd agents-backend
```

### 2. Database Setup

#### Create PostgreSQL Database
```sql
-- Connect to PostgreSQL (adjust host/port as needed)
psql -h localhost -U postgres

-- Create database
CREATE DATABASE agents_of_leap;
\c agents_of_leap

-- Load schema (from workspace root)
\i schema.sql
```

For an existing database created before authentication was added, apply the additive,
rerunnable migration with `\i auth_schema.sql` after `schema.sql`.

#### Verify Schema
```sql
\dt  -- Should show: admin, accounts, clients, orders, holdings, instruments, transactions, etc.
\dT  -- Should show: admin_role, account_status, order_type, asset_class, etc. (ENUM types)
```

### 3. Environment Configuration

#### Create `.env` file in project root
```bash
# .env (DO NOT COMMIT - it's in .gitignore)

# PostgreSQL Connection
DB_HOST=localhost
DB_PORT=5432
DB_NAME=agents_of_leap
DB_USER=postgres
DB_PASSWORD=your_secure_password

# Optional warehouse database used by analytics mappers
WAREHOUSE_DB_HOST=localhost
WAREHOUSE_DB_PORT=25432
WAREHOUSE_DB_NAME=test_db
WAREHOUSE_DB_USER=test
WAREHOUSE_DB_PASSWORD=your_warehouse_password

# Optional
JAVA_OPTS=-Xmx512m
```

#### Local-only application properties
For a local SSH tunnel, use the ignored [application-local.properties](application-local.properties) file. It is imported automatically by the tracked `application.properties` file and is not copied into the Docker image.

Set the values once:
```properties
spring.datasource.url=jdbc:postgresql://localhost:15432/agents_of_leap
spring.datasource.username=postgres
spring.datasource.password=your_secure_password
spring.datasource.warehouse.url=jdbc:postgresql://localhost:25432/test_db
spring.datasource.warehouse.username=test
spring.datasource.warehouse.password=your_warehouse_password
```

### JWT-protected Mission API

This Spring Boot application validates RS256 JWT signatures using a statically supplied RSA public PEM, and checks issuer, audience, and token time claims. The signing private key stays outside Spring. `/users/me` and protected `/api/**` routes require a valid JWT; role and resource-ownership rules are enforced by Spring Security and Mission service logic.

`GET /api/missions` does not exist in this project. Existing protected endpoints include `GET /api/accounts`, `GET /api/users/me`, and order routes under `/api/v1/orders`. `/api/users/me` needs a matching client row in PostgreSQL; account listing returns accounts scoped to the JWT subject.

### Local development JWT issuer (no virtual environment)

The repository includes `issuer.py`, a **local-only development helper**, not an authentication service and not production code. It generates a fresh RSA key pair in memory each time it starts, serves only the public key via JWKS, and requires `JWT_DEV_MODE=true` with a non-production `APP_ENV`. It binds to `127.0.0.1:3000`; no private key is written to disk. No virtual environment is needed: run it with system Python. Flask, cryptography, and PyJWT must be installed in that interpreter; `requirements-dev-issuer.txt` lists them if missing.

In PowerShell, start the issuer in terminal 1 from the repository root:

```powershell
$env:JWT_DEV_MODE = "true"
$env:APP_ENV = "development"
py issuer.py
```

To test the local `issuer.py` (which creates its own temporary signing key), start Spring in terminal 2 from the same root. Clear the static NestJS PEM setting so Spring uses this local issuer's JWKS instead. Spring imports the optional ignored root `.env`; process environment variables override it. Stop any old Spring process using port 8083 before restarting:

```powershell
$env:AUTH_JWT_JWKS_URI = "http://127.0.0.1:3000/.well-known/jwks.json"
$env:AUTH_JWT_ISSUER = "agents-middleware"
$env:AUTH_JWT_AUDIENCE = "agents-backend"
$env:JWT_PUBLIC_KEY_FILE = ""
$env:JWT_PUBLIC_KEY = ""
mvn spring-boot:run
```

For NestJS tokens, Spring uses only the RSA public key for RS256 verification, never the private key. Configure issuer and audience as `agents-middleware` and `agents-backend`, or override them to match NestJS's exact `iss` and `aud` claims. The local `issuer.py` workflow is obsolete for backend validation because access tokens are validated only with the statically supplied public key.

For a paired-token login response, use Bruno `POST http://127.0.0.1:3000/auth/login`, header `Content-Type: application/json`, body `{"email":"alex@example.com","password":"ClientTest123!"}`. The response contains camelCase `accessToken`, `refreshToken`, `expiresIn` (300 seconds), and a `user` object with `clientId`, `email`, `firstName`, `lastName`, and `role`. Keep the tokens in Bruno's private/local environment; do not paste them in chat or commit them. Use `accessToken` as Bearer auth for protected Spring requests. To rotate the refresh token, call `POST http://127.0.0.1:3000/auth/refresh` with `{"refreshToken":"{{refreshToken}}"}`; each refresh token is single-use and the replacement token must be saved.

For access-only role and negative-case tests, use Bruno `POST http://127.0.0.1:3000/dev/token` with `{"role":"CLIENT","case":"valid"}`. This endpoint returns snake_case `access_token`, `token_type`, and `expires_in`. The local test subject is `11111111-1111-4111-8111-111111111111`. The protected endpoint should authenticate as CLIENT; whether it returns account data depends on the database containing accounts for that subject.

The local issuer can create negative test tokens by setting `case` to `wrong-signature`, `wrong-issuer`, `wrong-audience`, `expired`, or `invalid-subject`. Spring should reject them with 401. This issuer is unsuitable for production: its key changes on restart, users/credentials are simulated, and its token endpoint is intentionally only for local development.

#### Test NestJS access tokens and client isolation in Bruno

1. Obtain an access token from the NestJS login endpoint. Keep the token local/private in Bruno; never commit it or send it in chat.
2. Call `GET http://localhost:8083/api/jwt/verify-header` with **Auth → Bearer Token** set to the access token. A valid signature, issuer, audience, and time window should return 200. Missing, expired, wrong-key/signature, wrong-issuer, or wrong-audience tokens should return 401.
3. Test role authorization with tokens minted by NestJS for each role. For example, a CLIENT token can access `/api/accounts`; an ADMIN-only endpoint such as `/api/admins/**` should reject that CLIENT token with 403. Spring currently maps `role` and `roles` claims with CLIENT, ADMIN, or ANALYST values (optionally prefixed with `ROLE_`); unsupported/missing roles do not grant a protected role.
4. To test tenant isolation, use two real client accounts in the database. With client A's token, `GET /api/accounts` must list only accounts belonging to A; `GET /api/accounts/{client-B-account-id}` must return 404. Likewise, requests for B's order history/order IDs must return 404, and B's watchlist client ID must be rejected with 403. The pending-orders endpoint requires an `accountId`, which is checked against the token subject before service access.

The account-ownership implementation currently interprets the JWT `sub` claim as the client's UUID. Confirm that NestJS sets `sub` to the same client UUID (or tell us the claim that contains the client UUID before using real tokens for account-scoped routes). Verification-only can succeed even if that identity-to-client mapping is not yet correct.
The local file is git-ignored. Do not put passwords in the tracked `src/main/resources/application.properties` file.

### 4. Build and Run

#### Clean Build
```powershell
mvn clean compile
```

#### Run Locally (Port 8083)
```powershell
mvn spring-boot:run
```

#### Package as JAR
```powershell
mvn clean package
java -jar target/agents-backend.jar
```

### 5. Verify Application
Application is running when you see in logs:
```
Tomcat started on port 8083 (http) with context path '/'
Started AgentsBackendApplication in 2.7 seconds
```

### Docker Database Configuration

The Docker container receives database configuration through environment variables. Do not copy `application.properties` or local credential files into the image. On the Linux VM, create an untracked file such as `/opt/agents-backend/agents-backend.env`:

```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=agents_of_leap
DB_USER=postgres
DB_PASSWORD=your_primary_password
WAREHOUSE_DB_HOST=warehouse-postgres
WAREHOUSE_DB_PORT=5432
WAREHOUSE_DB_NAME=test_db
WAREHOUSE_DB_USER=test
WAREHOUSE_DB_PASSWORD=your_warehouse_password
SERVER_PORT=8080
```

Use Docker service/container names for `DB_HOST` and `WAREHOUSE_DB_HOST`, not `localhost`. Run the backend on the same Docker network as the databases:

```bash
docker run -d \
  --name agents-backend \
  --network agents-network \
  --env-file /opt/agents-backend/agents-backend.env \
  --mount type=bind,src=/opt/agents-backend/secrets/public_key.pem,dst=/run/secrets/jwt_public_key.pem,readonly \
  -p 8080:8080 \
  agents-backend:latest
```

The image defaults to `/run/secrets/jwt_public_key.pem`, so no JWT key environment variable is needed in Docker. Place only the middleware's public key at `/opt/agents-backend/secrets/public_key.pem`; keep the private signing key in the middleware's separate secret store or container and never mount it into this backend. The environment file and mounted key stay on the VM and should not be committed.

### Test Coverage

Generate the JaCoCo HTML report with:

```powershell
mvn clean test
```

Open `target/site/jacoco/index.html` in a browser. On Windows:

```powershell
Start-Process .\target\site\jacoco\index.html
```

### Dashboard Bruno Request

The dashboard endpoint aggregates data for one client across active accounts:

- Active account balances and portfolio values
- Platform-wide market movers
- Client watchlist securities
- Five most recent orders across all statuses
- Daily portfolio history for up to 365 days

Request:

```http
GET http://localhost:8080/api/dashboard?clientId={{clientId}}&period=90D
Accept: application/json
```

This request has no body. Create a Bruno environment variable named `clientId` using the ID printed by `test-dashboard-data.sql`. Supported periods are `30D`, `90D`, `180D`, and `365D`.

Expected response sections are `accounts`, `marketMovers`, `watchlist`, `recentTrades`, and `portfolioHistory`.

Invalid UUIDs or periods return `400`. A successful response contains all five sections, including empty arrays when no rows exist. Database or mapper failures currently fail the aggregation with `500`; the endpoint does not fabricate partial data. If partial success is required later, isolate each section behind independent reads and add per-section status/error fields or use separate refresh endpoints.

Example response shape:

```json
{
  "accounts": [],
  "marketMovers": [],
  "watchlist": [],
  "recentTrades": [],
  "portfolioHistory": []
}
```

---

## Project Structure

### Monorepo Architecture
This repository contains the core trading backend service. Related services are maintained as separate repositories:

| Service | Repository | Purpose | Tech Stack |
|---------|------------|---------|------------|
| **agents-backend** (this repo) | [ahmedleap/agents-backend](https://github.com/ahmedleap/agents-backend) | Core trading platform (orders, accounts, holdings) | Spring Boot 3.3.1, Java 21, MyBatis, PostgreSQL |
| **agents-frontend** | [ahmedleap/agents-frontend](https://github.com/ahmedleap/agents-frontend) | Angular UI for traders (TBD) | Angular 17+, TypeScript, RxJS |
| **agents-middleware** | [ahmedleap/agents-middleware](https://github.com/ahmedleap/agents-middleware) | API Gateway, auth, rate limiting (TBD) | NestJS, Node.js, Express |
| **agents-ticker** | [ahmedleap/agents-ticker](https://github.com/ahmedleap/agents-ticker) | Real-time price feeds & market data | Python, FastAPI |
| Future services | TBD | Portfolio analytics, risk modeling, etc. | TBD |

### Backend Directory Layout
```
agents-backend/
├── pom.xml                          # Maven configuration (MyBatis, Spring Boot, PostgreSQL)
├── schema.sql                       # PostgreSQL schema (ENUMS + tables + indexes)
├── .env                             # Environment variables (local, git-ignored)
├── .gitignore                       # Git exclusions (target/, *.jar, build.ps1, etc.)
│
├── src/main/
│   ├── java/com/agentsbackend/
│   │   ├── AgentsBackendApplication.java    # Spring Boot entry point
│   │   │
│   │   ├── config/                          # MyBatis configuration & type handlers
│   │   │   ├── MyBatisTypeHandlerConfigurer.java  # Registers 9 type handlers
│   │   │   ├── UUIDTypeHandler.java         # UUID ↔ PostgreSQL UUID conversion
│   │   │   ├── AdminRoleTypeHandler.java    # AdminRole enum ↔ PostgreSQL ENUM
│   │   │   ├── OrderStatusTypeHandler.java  # OrderStatus enum ↔ PostgreSQL ENUM
│   │   │   ├── OrderTypeTypeHandler.java    # OrderType enum ↔ PostgreSQL ENUM
│   │   │   ├── AccountStatusTypeHandler.java
│   │   │   ├── AssetClassTypeHandler.java
│   │   │   ├── PortfolioSizeRangeTypeHandler.java
│   │   │   ├── RiskToleranceTypeHandler.java
│   │   │   └── TransactionTypeTypeHandler.java
│   │   │
│   │   ├── entities/                        # Pure POJOs (no JPA annotations)
│   │   │   ├── Account.java                 # Trading accounts
│   │   │   ├── Admin.java                   # Platform admins
│   │   │   ├── AuditLog.java                # Audit trail
│   │   │   ├── Client.java                  # Retail traders
│   │   │   ├── HistoricalSnapshot.java      # EOD portfolio snapshots
│   │   │   ├── Holding.java                 # Stock/ETF positions
│   │   │   ├── Instrument.java              # Securities (bid/ask/mid_price)
│   │   │   ├── InstrumentPrice.java         # Legacy pricing (deprecated)
│   │   │   ├── InstrumentPriceHistory.java  # Daily OHLCV bars (NEW - v0.5.1)
│   │   │   ├── Order.java                   # BUY/SELL orders (limit & market)
│   │   │   ├── Transaction.java             # Deposits/withdrawals
│   │   │   └── Watchlist.java               # Client watchlists
│   │   │
│   │   ├── enums/                           # Java enums (mapped to PostgreSQL ENUM types)
│   │   │   ├── AdminRole.java
│   │   │   ├── AccountStatus.java
│   │   │   ├── AssetClass.java
│   │   │   ├── OrderStatus.java
│   │   │   ├── OrderType.java
│   │   │   ├── PortfolioSizeRange.java
│   │   │   ├── RiskTolerance.java
│   │   │   └── TransactionType.java
│   │   │
│   │   ├── repos/                           # MyBatis @Mapper interfaces (SQL layer)
│   │   │   ├── AccountRepository.java       # Account CRUD & queries
│   │   │   ├── AdminRepository.java         # Admin CRUD
│   │   │   ├── AuditTrailRepository.java    # Audit log persistence
│   │   │   ├── ClientRepository.java        # Client CRUD
│   │   │   ├── HoldingRepository.java       # Holdings CRUD & calculations
│   │   │   ├── InstrumentRepository.java    # Instrument/pricing CRUD
│   │   │   ├── InstrumentPriceHistoryRepository.java  # Daily bars (NEW - v0.5.1)
│   │   │   ├── OrderRepository.java         # Order CRUD & fulfillment
│   │   │   ├── TransactionRepository.java   # Transaction CRUD
│   │   │   └── WatchlistRepository.java     # Watchlist CRUD
│   │   │
│   │   ├── services/                        # Business logic layer (MyBatis agnostic)
│   │   │   ├── AccountService.java          # Account management interface
│   │   │   ├── AccountServiceImpl.java       # Deposits, withdrawals, balance queries
│   │   │   ├── AdminService.java            # Admin management interface
│   │   │   ├── AdminServiceImpl.java         # Admin CRUD operations
│   │   │   ├── AuditTrailService.java       # Audit logging
│   │   │   ├── HoldingsService.java         # Holdings management
│   │   │   ├── OrderFulfillmentService.java # Market order processing & execution
│   │   │   ├── OrderService.java            # Order management interface
│   │   │   ├── OrderServiceImpl.java         # Order CRUD & lifecycle
│   │   │   ├── WatchlistService.java        # Watchlist management
│   │   │   ├── ClientService.java           # Client profile management
│   │   │   └── queue/OrderQueue.java        # Order processing queue
│   │   │
│   │   ├── controllers/                     # REST API layer (@RestController)
│   │   │   ├── AccountController.java       # /api/accounts (PATCH deposit/withdraw)
│   │   │   ├── AdminController.java         # /api/admins (POST create)
│   │   │   ├── OrderController.java         # /api/v1/orders (DELETE cancel)
│   │   │   ├── WatchlistController.java     # /api/v2/watchlists (already RESTful)
│   │   │   └── ClientController.java        # /api/clients (TBD)
│   │   │
│   │   ├── DTO/                             # Data Transfer Objects
│   │   │   ├── requests/                    # @RequestBody models
│   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   ├── CancelOrderRequest.java
│   │   │   │   ├── CreateAdminRequest.java
│   │   │   │   ├── AccountsRequest.java     # Nested: Deposit, Withdrawal
│   │   │   │   └── GetPendingOrdersRequest.java
│   │   │   │
│   │   │   └── response/                    # @ResponseBody models
│   │   │       ├── CreateOrderResponse.java
│   │   │       ├── CancelOrderResponse.java
│   │   │       ├── OrderSummaryResponse.java
│   │   │       ├── AccountsResponse.java    # Nested: Transaction
│   │   │       └── AccountDetailsResponse.java
│   │   │
│   │   ├── exceptions/                      # Custom exception types
│   │   │   ├── OrderNotFoundException.java
│   │   │   ├── InvalidOrderParametersException.java
│   │   │   ├── InsufficientFundsException.java
│   │   │   └── AccountNotFoundException.java
│   │   │
│   │   └── queue/                           # Order processing queue
│   │       ├── OrderQueue.java              # Interface for queue
│   │       └── InMemoryOrderQueue.java      # In-memory implementation
│   │
│   └── resources/
│       └── application.properties            # Spring Boot configuration
│
└── target/                          # Compiled classes (git-ignored)
```

---

## Architecture Overview

### Why MyBatis (Not JPA)?
This project uses **MyBatis** for database access:
- **Explicit SQL:** Clear, debuggable queries (no ORM magic)
- **Type Handlers:** Custom handlers for UUID and Enum conversions
- **Type Safety:** Compile-time checking of SQL parameters
- **Performance:** Direct SQL mapping without ORM overhead

### Related Services & Repositories
When building features that depend on external services:

**Frontend:**
- **[agents-frontend](https://github.com/ahmedleap/agents-frontend)** (TBD)
  - Angular 17+ UI for traders
  - TypeScript, RxJS for reactive programming
  - Consumes REST APIs from agents-backend through middleware

**API Gateway & Middleware:**
- **[agents-middleware](https://github.com/ahmedleap/agents-middleware)** (TBD)
  - NestJS + Node.js API Gateway
  - Authentication, authorization, rate limiting
  - Request validation & error handling
  - Routes: `/api/*` → agents-backend, `/auth/*` → auth service

**Market Data:**
- **[agents-ticker](https://github.com/ahmedleap/agents-ticker)** - Real-time price feeds
  - Provides bid/ask prices via Requests
  - Updates `Instrument.bid`, `Instrument.ask`, `Instrument.priceUpdatedAt`
  - Called from `OrderFulfillmentService` for live order fulfillment

**Future Services:**
- Portfolio analytics microservice
- Risk modeling microservice
- Historical analysis & reporting microservice

---

## Kafka Integration - Asynchronous Order Processing

### Overview
The backend supports both **in-memory queue** and **Apache Kafka** for order fulfillment processing. Switch between them via a single configuration property.

### Architecture

```
API Request (POST /api/v1/orders)
    ↓
OrderServiceImpl.createOrder()
    ├─ Validate order
    ├─ Save to database
    └─ Call OrderSubmissionService.submitOrder()
                    ↓
        ┌───────────┴────────────┐
        ↓                         ↓
    (queue mode)           (kafka mode)
        ↓                         ↓
QueueOrderSubmissionService  KafkaOrderSubmissionService
        ↓                         ↓
    OrderQueue           Kafka Topic: order-fulfillment
    (in-memory)         (accountId as partition key)
        ↓                         ↓
OrderFulfillmentService     OrderConsumerService
  @Scheduled every 5s       @KafkaListener
        ↓                         ↓
  Poll queue ─────────→  Consume from broker
        ↓                         ↓
  tryFillOrder() ────────→ tryFillOrder()
  fillOrder()            fillOrder()
```

### Configuration

#### Enable Kafka Mode
Edit `src/main/resources/application.properties`:

```properties
# Order processing mode: "queue" (default) or "kafka"
order.processing.mode=kafka

# Kafka broker connection
spring.kafka.bootstrap-servers=<IP_ADDRESSE>:9092
spring.kafka.consumer.group-id=order-fulfillment-group
spring.kafka.consumer.auto-offset-reset=earliest

# Serialization
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
```

#### Switch Back to Queue Mode
```properties
order.processing.mode=queue
```

### Kafka Setup

#### Create Topic on Kafka Broker
```bash
# SSH to Linux box with Kafka
ssh user@<IP_ADDRESS>

# Create topic with 3 partitions and replication factor 3
kafka-topics.sh --bootstrap-server localhost:9092 \
  --create \
  --topic order-fulfillment \
  --partitions 3 \
  --replication-factor 3

# Verify topic created
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

#### Topic Configuration Details
- **Topic Name:** `order-fulfillment`
- **Partitions:** 3 (allows parallel processing across 3 consumer instances)
- **Replication Factor:** 3 (high availability; requires 3+ brokers)
- **Partition Key:** `accountId` (ensures orders from same account stay in order)

### How It Works

#### Producer (KafkaOrderSubmissionService)
1. Order created via API → published to Kafka immediately
2. Uses **accountId as partition key** for ordering guarantees
3. All orders for Account A → Partition 0 (FIFO)
4. All orders for Account B → Partition 1 (FIFO)
5. Parallel accounts don't interfere with each other

#### Consumer (OrderConsumerService)
1. Listens to `order-fulfillment` topic
2. **Concurrency: 3** (processes up to 3 partitions in parallel)
3. For each order:
   - Checks current instrument bid/ask prices
   - Determines if order can be filled
   - If filled: updates order, cash, holdings, audit trail
   - If not filled: order remains PENDING (retry later)

#### Key Design Decisions
- **Per-Account Ordering:** accountId as partition key → same account orders always in order
- **Parallel Processing:** Different accounts processed simultaneously across partitions
- **Idempotent Consumption:** Safe to replay messages (updates are idempotent)
- **Auto-offset Commit:** Consumer tracks position automatically

### Monitoring & Debugging

#### View Logs
```bash
# Tail application logs
tail -f logs/application.log

# Look for these messages:
# Producer:
# "Order [uuid] published to Kafka topic: order-fulfillment with partition key: [accountId]"

# Consumer:
# "Received order [uuid] from Kafka"
# "Order [uuid] filled successfully"
# OR "Order [uuid] could not be filled at current market price"
```

#### Check Kafka Messages
```bash
# Inside Kafka container, consume messages from topic
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-fulfillment \
  --from-beginning \
  --max-messages 10
```

#### Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.org.springframework.kafka=DEBUG
logging.level.org.apache.kafka=DEBUG
logging.level.com.agentsbackend.services=DEBUG
```

### Files Created

#### Strategy Pattern (Interfaces & Implementations)
- **`OrderSubmissionService.java`** - Interface defining `submitOrder(Order)`
- **`QueueOrderSubmissionService.java`** - Queue-based implementation
- **`KafkaOrderSubmissionService.java`** - Kafka-based implementation

#### Kafka Configuration
- **`KafkaProducerConfig.java`** - Configures KafkaTemplate with JSON serialization
- **`KafkaConsumerConfig.java`** - Configures consumer factory with 3-partition concurrency

#### Consumer
- **`OrderConsumerService.java`** - Listens to Kafka and fulfills orders
  - Only active when `order.processing.mode=kafka`
  - Contains: `tryFillOrder()`, `fillOrder()`, cash/holdings updates

#### Tests
- **`KafkaOrderSubmissionServiceTest.java`** - Tests producer publishing
- **`OrderConsumerServiceTest.java`** - Tests consumer fulfillment logic
- **`QueueOrderSubmissionServiceTest.java`** - Tests queue submission
- **`OrderServiceImplTest.java`** - Updated to mock OrderSubmissionService

### Switching Modes

#### From Queue to Kafka
1. Set `order.processing.mode=kafka` in `application.properties`
2. Create `order-fulfillment` topic on Kafka broker
3. Restart Spring Boot app
4. OrderConsumerService bean loads automatically

#### From Kafka Back to Queue
1. Set `order.processing.mode=queue` in `application.properties`
2. Restart Spring Boot app
3. OrderFulfillmentService resumes polling queue every 5 seconds

### Performance Characteristics

| Aspect | Queue | Kafka |
|--------|-------|-------|
| Latency | Poll every 5s | Near real-time |
| Scaling | Single instance | Multiple consumers |
| Ordering | Per-account | Per-partition (accounts) |
| Durability | In-memory only | Persisted on broker |
| Failure Recovery | Lost on restart | Replayed from broker |
| Concurrency | Sequential polling | 3 partitions in parallel |

---

## References

- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [Spring Boot MyBatis Starter](https://github.com/mybatis/spring-boot-starter)
- [PostgreSQL ENUM Types](https://www.postgresql.org/docs/current/datatype-enum.html)
- [PostgreSQL UUID Type](https://www.postgresql.org/docs/current/datatype-uuid.html)