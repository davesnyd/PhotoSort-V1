# PhotoSort Backend Dockerfile
# Copyright 2025, David Snyderman
#
# Multi-stage build for Spring Boot application with Python/STAG support
# Stage 1: Build the Java application
# Stage 2: Run the application with Python for AI tagging

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

# Stage 2: Runtime (Debian-based for Python compatibility)
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Install Python and dependencies for STAG
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    python3-venv \
    git \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Create virtual environment for STAG
RUN python3 -m venv /app/stag-venv

# Copy STAG wrapper requirements and install
COPY stag-wrapper/requirements.txt /app/stag-wrapper/requirements.txt
RUN /app/stag-venv/bin/pip install --no-cache-dir -r /app/stag-wrapper/requirements.txt

# Copy STAG wrapper script
COPY stag-wrapper/stag_single.py /app/stag-wrapper/stag_single.py
RUN chmod +x /app/stag-wrapper/stag_single.py

# Create non-root user for security
RUN groupadd -r photosort && useradd -r -g photosort photosort

# Copy JAR from builder stage
COPY --from=builder /build/target/PhotoSortServices-*.jar /app/photosort-backend.jar

# Create directories for logs, STAG, and Hugging Face cache
RUN mkdir -p /app/logs /app/stag /app/hf-cache && chown -R photosort:photosort /app

# Set environment variables for STAG and Hugging Face
ENV STAG_DIR=/app/stag
ENV HF_HOME=/app/hf-cache
ENV TRANSFORMERS_CACHE=/app/hf-cache

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
