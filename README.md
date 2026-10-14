# Agents Backend - Spring Boot + MyBatis Trading Platform

A high-performance Java backend for a trading/investment platform using Spring Boot, MyBatis, and PostgreSQL.

## Technology Stack

- **Java 21** (LTS - Temurin/Adoptium)
- **Spring Boot 3.3.1** (Web, no JPA)
- **MyBatis 3.0.3** (SQL Mapper, annotation-based)
- **PostgreSQL 42.6.0** (Database driver)
- **Maven 3.9.9** (Build tool)

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

# JWT verification configuration for the local development issuer
AUTH_JWT_JWKS_URI=http://127.0.0.1:3000/.well-known/jwks.json
AUTH_JWT_ISSUER=agents-middleware
AUTH_JWT_AUDIENCE=agents-backend
JWT_DEV_MODE=true

# Optional
JAVA_OPTS=-Xmx512m
```

#### Or use `application.properties` directly
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agents_of_leap
spring.datasource.username=postgres
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=validate
mybatis.configuration.map-underscore-to-camel-case=true

logging.level.com.agentsbackend=DEBUG
```

### JWT-protected Mission API

This Spring Boot application validates RS256 JWT signatures using the configured public PEM or JWKS, and checks issuer, audience, and token time claims. The signing private key stays outside Spring. `/users/me` and protected `/api/**` routes require a valid JWT; role and resource-ownership rules are enforced by Spring Security and Mission service logic.

`GET /api/missions` does not exist in this project. Existing protected endpoints include `GET /api/accounts`, `GET /users/me`, and order routes under `/api/v1/orders`. `/users/me` needs a matching client row in PostgreSQL; account listing returns accounts scoped to the JWT subject.

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

For NestJS tokens, Spring reads the NestJS RSA public PEM from the workspace-root `public_key.pem` by default. Set `JWT_PUBLIC_KEY_FILE` to another path if needed; `JWT_PUBLIC_KEY` is an optional Base64-encoded PEM fallback. Spring uses only the public key for RS256 verification, never the private key. Configure issuer and audience as `agents-middleware` and `agents-backend`, or override them to match NestJS's exact `iss` and `aud` claims. Do not use the local `issuer.py` tokens in this mode: that helper generates a different ephemeral key.

For a paired-token login response, use Bruno `POST http://127.0.0.1:3000/auth/login`, header `Content-Type: application/json`, body `{"email":"alex@example.com","password":"ClientTest123!"}`. The response contains camelCase `accessToken`, `refreshToken`, `expiresIn` (300 seconds), and a `user` object with `clientId`, `email`, `firstName`, `lastName`, and `role`. Keep the tokens in Bruno's private/local environment; do not paste them in chat or commit them. Use `accessToken` as Bearer auth for protected Spring requests. To rotate the refresh token, call `POST http://127.0.0.1:3000/auth/refresh` with `{"refreshToken":"{{refreshToken}}"}`; each refresh token is single-use and the replacement token must be saved.

For access-only role and negative-case tests, use Bruno `POST http://127.0.0.1:3000/dev/token` with `{"role":"CLIENT","case":"valid"}`. This endpoint returns snake_case `access_token`, `token_type`, and `expires_in`. The local test subject is `11111111-1111-4111-8111-111111111111`. The protected endpoint should authenticate as CLIENT; whether it returns account data depends on the database containing accounts for that subject.

The local issuer can create negative test tokens by setting `case` to `wrong-signature`, `wrong-issuer`, `wrong-audience`, `expired`, or `invalid-subject`. Spring should reject them with 401. This issuer is unsuitable for production: its key changes on restart, users/credentials are simulated, and its token endpoint is intentionally only for local development.

#### Test NestJS access tokens and client isolation in Bruno

1. Obtain an access token from the NestJS login endpoint. Keep the token local/private in Bruno; never commit it or send it in chat.
2. Call `GET http://localhost:8083/api/jwt/verify-header` with **Auth → Bearer Token** set to the access token. A valid signature, issuer, audience, and time window should return 200. Missing, expired, wrong-key/signature, wrong-issuer, or wrong-audience tokens should return 401.
3. Test role authorization with tokens minted by NestJS for each role. For example, a CLIENT token can access `/api/accounts`; an ADMIN-only endpoint such as `/api/admins/**` should reject that CLIENT token with 403. Spring currently maps `role` and `roles` claims with CLIENT, ADMIN, or ANALYST values (optionally prefixed with `ROLE_`); unsupported/missing roles do not grant a protected role.
4. To test tenant isolation, use two real client accounts in the database. With client A's token, `GET /api/accounts` must list only accounts belonging to A; `GET /api/accounts/{client-B-account-id}` must return 404. Likewise, requests for B's order history/order IDs must return 404, and B's watchlist client ID must be rejected with 403. The pending-orders endpoint requires an `accountId`, which is checked against the token subject before service access.

The account-ownership implementation currently interprets the JWT `sub` claim as the client's UUID. Confirm that NestJS sets `sub` to the same client UUID (or tell us the claim that contains the client UUID before using real tokens for account-scoped routes). Verification-only can succeed even if that identity-to-client mapping is not yet correct.

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

## References

- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [Spring Boot MyBatis Starter](https://github.com/mybatis/spring-boot-starter)
- [PostgreSQL ENUM Types](https://www.postgresql.org/docs/current/datatype-enum.html)
- [PostgreSQL UUID Type](https://www.postgresql.org/docs/current/datatype-uuid.html)