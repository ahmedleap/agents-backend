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