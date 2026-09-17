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