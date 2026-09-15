# Admin API Testing Guide

## Prerequisites
- Make sure the Spring Boot application is running (default port: 8080)
- Database must be configured and running

## Test Create Admin Endpoint

### Using cURL:
```bash
curl -X POST http://localhost:8080/api/admins/create \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "passwordHash": "$2a$10$hashedpassword123456789",
    "role": "ADMIN",
    "createdAt": "2026-09-15T10:30:00"
  }'
```

### Using Postman:
1. Create a new POST request
2. URL: `http://localhost:8080/api/admins/create`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "passwordHash": "$2a$10$hashedpassword987654321",
  "role": "ANALYST",
  "createdAt": "2026-09-15T11:00:00"
}
```

### Expected Response (201 Created):
```json
{
  "adminId": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "passwordHash": "$2a$10$hashedpassword123456789",
  "role": "ADMIN",
  "createdAt": "2026-09-15T10:30:00"
}
```

## Configuration Changes Needed

Add to `application.yml` or `application.properties`:

### application.yml:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/agents_db
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQL10Dialect

mybatis:
  configuration:
    map-underscore-to-camel-case: true
    jdbc-type-for-null: NULL
```

### application.properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agents_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.jdbc-type-for-null=NULL
```

## Maven Dependencies (Add to pom.xml if not already present):
```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.2</version>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
    <scope>runtime</scope>
</dependency>
```
