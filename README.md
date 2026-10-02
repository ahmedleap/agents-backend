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

### 4. Build and Run

#### Clean Build
```powershell
mvn clean compile
```

#### Run Locally (Port 8080)
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
Tomcat started on port 8080 (http) with context path '/'
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