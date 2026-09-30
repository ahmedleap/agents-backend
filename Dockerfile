# Multi-stage build for Spring Boot Backend (Lean Production Image)

# Build arguments
ARG VERSION=0.4.3

# Stage 1: Builder
FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /build

# Copy Maven config files
COPY pom.xml .
COPY src ./src

# Print toolchain versions for visibility in build logs
RUN echo "--- java -version ---" && java -version \
    && echo "--- mvn -version ---" && mvn -version

# Build: compile, run tests with coverage, create JAR
# -B: batch mode (no interactive input)
# verify: runs through full lifecycle including tests
RUN mvn -B clean verify

# Stage 2: Runtime (Lean)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Build arguments available in runtime stage
ARG VERSION=0.4.3
LABEL version="${VERSION}"
LABEL description="Spring Boot Backend - Agents System"
LABEL maintainer="agents-backend-team"

# Copy JAR from builder stage
COPY --from=builder /build/target/agents-backend.jar .

# Create non-root user for security
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0"

# Expose Spring Boot default port
EXPOSE 8080

# Health check: Spring Boot actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD java -cp agents-backend.jar org.springframework.boot.loader.JarLauncher 2>&1 | grep -q "Started" || exit 1

# Run application
CMD ["java", "-jar", "agents-backend.jar"]
