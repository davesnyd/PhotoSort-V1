# PhotoSort Backend Dockerfile
# Copyright 2025, David Snyderman
#
# Multi-stage build for Spring Boot application
# Stage 1: Build the application
# Stage 2: Run the application

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests in Docker build - run separately)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S photosort && adduser -S photosort -G photosort

# Copy JAR from builder stage
COPY --from=builder /build/target/PhotoSortServices-*.jar /app/photosort-backend.jar

# Create directory for logs
RUN mkdir -p /app/logs && chown -R photosort:photosort /app

# Switch to non-root user
USER photosort

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Environment variables (override with docker-compose or -e flags)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/photosort-backend.jar"]
