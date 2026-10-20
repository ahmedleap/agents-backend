# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Spring Boot backend (agents-backend): multi-stage build, lean runtime image
# ---------------------------------------------------------------------------

ARG JAVA_VERSION=21

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-${JAVA_VERSION} AS builder

WORKDIR /build

# Resolve dependencies first. This layer is cached until pom.xml changes,
# so code-only changes rebuild in seconds instead of re-downloading everything.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build the jar. Tests are skipped by default so the image build doesn't need a
# database or Kafka; run them in CI (Jenkins). Override with:
#   docker build --build-arg SKIP_TESTS=false .
ARG SKIP_TESTS=true
COPY src ./src
RUN mvn -B clean package -DskipTests=${SKIP_TESTS}

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:${JAVA_VERSION}-jre-jammy

ARG VERSION=0.0.0
LABEL version="${VERSION}" \
      description="Spring Boot Backend - Agents System" \
      maintainer="agents-backend-team"

# tzdata for scheduled tasks, curl for the healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends tzdata curl \
    && rm -rf /var/lib/apt/lists/*

# Non-root user; /run/secrets is where the JWT public key gets mounted
RUN useradd -m -u 1000 appuser \
    && mkdir -p /run/secrets \
    && chown appuser:appuser /run/secrets

WORKDIR /app
COPY --from=builder --chown=appuser:appuser /build/target/agents-backend.jar app.jar

USER appuser

ENV TZ=America/New_York \
    SPRING_PROFILES_ACTIVE=production \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0" \
    JWT_PUBLIC_KEY_FILE=/run/secrets/jwt_public_key.pem

EXPOSE 8080

# Needs spring-boot-starter-actuator. The port follows SERVER_PORT, so changing
# it in your env file can't leave the healthcheck pointing at the wrong place.
# If you set server.servlet.context-path, add it before /actuator.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fs http://localhost:${SERVER_PORT}/actuator/health || exit 1

# exec so the JVM is PID 1 and receives SIGTERM for a graceful shutdown
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]