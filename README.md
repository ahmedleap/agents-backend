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

```
agents-backend/
├── pom.xml                          # Maven configuration
├── schema.sql                       # PostgreSQL schema (ENUMS + tables + indexes)
├── .env                             # Environment variables (local, git-ignored)
├── .gitignore                       # Git exclusions (target/, *.jar, build.ps1, etc.)
│
├── src/main/
│   ├── java/com/agentsbackend/
│   │   ├── AgentsBackendApplication.java    # Spring Boot entry point
│   │   │
│   │   ├── config/                          # MyBatis configuration
│   │   │   ├── UUIDTypeHandler.java         # UUID ↔ PostgreSQL UUID
│   │   │   ├── AdminRoleTypeHandler.java    # Enum ↔ PostgreSQL ENUM
│   │   │   └── MyBatisTypeHandlerConfigurer.java  # Handler registration
│   │   │
│   │   ├── entities/                        # JPA @Entity beans (getters/setters only)
│   │   │   ├── Admin.java
│   │   │   ├── Account.java
│   │   │   ├── Client.java
│   │   │   ├── Order.java
│   │   │   ├── Holding.java
│   │   │   ├── Instrument.java
│   │   │   ├── InstrumentPrice.java
│   │   │   ├── Transaction.java
│   │   │   └── HistoricalSnapshot.java
│   │   │
│   │   ├── enums/                           # Java enums (AdminRole, OrderStatus, etc.)
│   │   │   ├── AdminRole.java
│   │   │   ├── AccountStatus.java
│   │   │   ├── AssetClass.java
│   │   │   ├── OrderStatus.java
│   │   │   ├── OrderType.java
│   │   │   ├── TransactionType.java
│   │   │   ├── RiskTolerance.java
│   │   │   └── PortfolioSizeRange.java
│   │   │
│   │   ├── repos/                           # MyBatis @Mapper interfaces
│   │   │   ├── AdminRepository.java         # SQL annotations + type hints
│   │   │   └── (OrderRepository, etc. - TBD)
│   │   │
│   │   ├── services/                        # Business logic layer
│   │   │   ├── AdminService.java            # Interface
│   │   │   └── AdminServiceImpl.java         # Implementation
│   │   │
│   │   └── controllers/                     # REST API endpoints
│   │       └── AdminController.java         # @RestController, @RequestMapping
│   │
│   └── resources/
│       └── application.properties            # Spring Boot configuration
│
└── target/                          # Compiled classes (git-ignored)
```

---

## Development Workflow

### Creating New Features
When adding new entities, repositories, and services:
1. Define `@Entity` class with getters/setters (no Lombok)
2. Create `@Mapper` interface with SQL annotations
3. Implement `@Service` with business logic
4. Create `@RestController` with API endpoints
5. Test with Bruno or curl

---

## Key Design Decisions

### Why MyBatis Instead of JPA/Hibernate?
- **Performance**: Direct SQL control, no ORM overhead
- **Simplicity**: Clear SQL mapping, easier debugging
- **Predictability**: No magical query generation

### Why Exclude JPA Auto-Configuration?
Spring Boot auto-includes JPA (HibernateJpaAutoConfiguration) even if not used:
```java
@SpringBootApplication(exclude = { 
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
```
This prevents unnecessary Spring beans and Hibernate initialization.

### Why Type Handlers?
PostgreSQL has **native types** that don't map directly to Java:
- **UUID**: PostgreSQL `UUID` column ≠ Java `String`
- **ENUM**: PostgreSQL `admin_role` enum ≠ Java `String`

Type handlers automatically convert between them at the MyBatis layer:
```java
// Automatic conversion
Admin admin = new Admin();
admin.setAdminId(UUID.randomUUID());  // Java UUID object
admin.setRole(AdminRole.ADMIN);        // Java enum

adminRepository.create(admin);         // Type handlers convert automatically
// PostgreSQL receives: native UUID + native ENUM type
```

---

## Dependency Issues & Solutions

### Issue 1: Lombok + Java 25 Incompatibility
**Problem**: Lombok 1.18.30+ doesn't support Java 25's javac internal APIs (TypeTag :: UNKNOWN error)
**What Changed**: Removed Lombok dependency from `pom.xml` and all entity classes
**Why**: Lombok's annotation processor broke on Java 25; manually adding getters/setters via IDE is more stable

### Issue 2: Spring Boot Auto-Including JPA
**Problem**: `spring-boot-starter-web` transitively pulls `spring-boot-starter-data-jpa`, initializing Hibernate despite using MyBatis-only
**What Changed**: Added exclusions to `@SpringBootApplication` annotation
**Why**: Prevent unnecessary Hibernate initialization and Spring beans that conflict with MyBatis

### Issue 3: MyBatis 3.0.2 Bean Factory Issues
**Problem**: MyBatis 3.0.2 had incompatibilities with Spring 6.1.10 (IllegalArgumentException on bean factory setup)
**What Changed**: Upgraded MyBatis dependency from 3.0.2 to 3.0.3 in `pom.xml`
**Why**: 3.0.3 fixed Spring 6.1.10 compatibility issues with bean registration

### Issue 4: Spring Boot 3.3.1 + Java 25 Bytecode Mismatch
**Problem**: Spring Boot 3.3.1 ASM library can't parse Java 25 bytecode (major version 69)
**What Changed**: Changed compilation target from Java 25 to Java 21 in `pom.xml` properties
**Why**: Spring Boot 3.3.1 was released before Java 25 support; Java 21 bytecode runs fine on Java 25 JVM (forward compatible)

### Issue 5: PostgreSQL UUID Type Mismatch
**Problem**: MyBatis was passing UUIDs as VARCHAR strings, PostgreSQL UUID column expected native UUID type
**What Changed**: Created `UUIDTypeHandler.java` and registered it in `MyBatisTypeHandlerConfigurer.java`
**Why**: Type handlers are MyBatis standard practice for custom type conversions; ensures UUID is passed as native type to PostgreSQL

### Issue 6: PostgreSQL ENUM Type Mismatch
**Problem**: MyBatis was passing enums as VARCHAR strings, PostgreSQL admin_role ENUM column expected explicit CAST
**What Changed**: Created `AdminRoleTypeHandler.java`, added `CAST()` to SQL queries, added explicit `@Param` annotations
**Why**: PostgreSQL ENUMs are distinct types; requires explicit casting. Type handlers handle Java enum ↔ string conversion, SQL handles string → ENUM cast

---

## Testing the API with Bruno

### Create Admin Endpoint
```
POST http://localhost:8080/api/admins/create
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "passwordHash": "hashed_password_here",
  "role": "ADMIN"
}
```

Service auto-generates `adminId` (UUID) and `createdAt` (timestamp).

---

## References

- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [Spring Boot MyBatis Starter](https://github.com/mybatis/spring-boot-starter)
- [PostgreSQL ENUM Types](https://www.postgresql.org/docs/current/datatype-enum.html)
- [PostgreSQL UUID Type](https://www.postgresql.org/docs/current/datatype-uuid.html)