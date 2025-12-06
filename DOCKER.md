# PhotoSort - Docker Deployment Guide

**Copyright 2025, David Snyderman**

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start](#quick-start)
4. [Configuration](#configuration)
5. [Building Images](#building-images)
6. [Running Containers](#running-containers)
7. [Container Management](#container-management)
8. [Data Persistence](#data-persistence)
9. [Networking](#networking)
10. [Troubleshooting](#troubleshooting)
11. [Production Deployment](#production-deployment)

---

## Overview

PhotoSort uses Docker containerization for easy deployment and consistency across environments. The application consists of three services:

- **PostgreSQL Database**: Data storage
- **Spring Boot Backend**: REST API and business logic
- **React Frontend**: User interface (served by nginx)

All services are orchestrated using Docker Compose with automatic health checks, persistent volumes, and inter-container networking.

---

## Prerequisites

### Required Software
```bash
docker --version      # 20.10 or higher
docker-compose --version  # 1.29 or higher
```

### Installation

**Ubuntu/Debian**:
```bash
# Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER  # Log out and back in

# Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

**macOS**:
```bash
# Install Docker Desktop from https://www.docker.com/products/docker-desktop
# Docker Compose is included
```

---

## Quick Start

### 1. Configure Environment
```bash
# Copy template
cp .env.docker.template .env

# Edit .env and fill in your credentials
nano .env
```

Required configuration:
- `DB_PASSWORD`: Secure database password
- `OAUTH_CLIENT_ID`: Google OAuth client ID
- `OAUTH_CLIENT_SECRET`: Google OAuth client secret

### 2. Build and Deploy
```bash
# Option 1: Using convenience script
./scripts/deploy-docker.sh

# Option 2: Manual steps
./scripts/build-docker.sh
docker-compose up -d
```

### 3. Access Application
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Database: localhost:5432

---

## Configuration

### Environment File (.env)

The `.env` file controls all configuration. See `.env.docker.template` for full options.

**Essential Settings**:
```bash
# Database
DB_NAME=PhotoSortData
DB_USERNAME=photosort_user
DB_PASSWORD=your_secure_password_here

# OAuth
OAUTH_CLIENT_ID=your_google_client_id
OAUTH_CLIENT_SECRET=your_google_client_secret

# Ports
BACKEND_PORT=8080
FRONTEND_PORT=3000
DB_PORT=5432
```

**Optional Settings**:
```bash
# Git repository (for photo polling feature)
GIT_REPO_URL=https://github.com/username/photos.git
GIT_USERNAME=your_username
GIT_TOKEN=your_github_token

# JPA settings
JPA_DDL_AUTO=update  # validate for production
JPA_SHOW_SQL=false   # true for debugging

# Java memory
JAVA_OPTS=-Xmx512m -Xms256m
```

### OAuth Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Create OAuth 2.0 Client ID (Web application)
3. Configure authorized URIs:
   - JavaScript origins: `http://localhost:8080`
   - Redirect URIs: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID and Secret to `.env`

---

## Building Images

### Using Build Script
```bash
./scripts/build-docker.sh
```

This script:
1. Checks for `.env` configuration
2. Builds backend image (Spring Boot)
3. Builds frontend image (React + nginx)
4. Lists all built images

### Manual Build
```bash
# Build all images
docker-compose build

# Build specific service
docker-compose build backend
docker-compose build frontend

# Build without cache (clean build)
docker-compose build --no-cache

# Build with custom API URL
docker-compose build --build-arg REACT_APP_API_BASE_URL=https://api.example.com frontend
```

### Image Details

**Backend Image** (photosort-backend):
- Base: eclipse-temurin:17-jre-alpine
- Size: ~300MB
- Multi-stage build (Maven build + JRE runtime)
- Non-root user for security
- Health check on `/actuator/health`

**Frontend Image** (photosort-frontend):
- Base: nginx:1.25-alpine
- Size: ~50MB
- Multi-stage build (Node build + nginx runtime)
- Optimized nginx configuration
- Health check on `/health`

---

## Running Containers

### Using Scripts

**Start**:
```bash
./scripts/start.sh
```

**Stop**:
```bash
./scripts/stop.sh
```

**Full Deployment**:
```bash
./scripts/deploy-docker.sh
```

### Docker Compose Commands

**Start all services**:
```bash
docker-compose up -d
```

**Start specific service**:
```bash
docker-compose up -d backend
```

**View logs**:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend

# Last 100 lines
docker-compose logs --tail=100 backend
```

**Check status**:
```bash
docker-compose ps
```

**Restart services**:
```bash
# Restart all
docker-compose restart

# Restart specific
docker-compose restart backend
```

**Stop and remove**:
```bash
# Stop containers
docker-compose down

# Stop and remove volumes (DELETES ALL DATA)
docker-compose down -v
```

---

## Container Management

### Shell Access

**Backend**:
```bash
docker-compose exec backend sh

# Run specific command
docker-compose exec backend ls -la /app
docker-compose exec backend java -version
```

**Frontend**:
```bash
docker-compose exec frontend sh

# Check nginx config
docker-compose exec frontend nginx -t
```

**Database**:
```bash
# PostgreSQL shell
docker-compose exec database psql -U photosort_user -d PhotoSortData

# Run SQL file
docker-compose exec -T database psql -U photosort_user -d PhotoSortData < backup.sql
```

### Health Checks

All services have automatic health checks. View status:
```bash
docker-compose ps
```

Healthy services show: `(healthy)`

**Health Check Endpoints**:
- Backend: `http://localhost:8080/actuator/health`
- Frontend: `http://localhost:3000/health`
- Database: `pg_isready` command

### Resource Limits

Add resource limits to `docker-compose.yml`:
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          memory: 512M
```

---

## Data Persistence

### Named Volumes

PhotoSort uses three named volumes for persistent data:

1. **postgres_data**: Database files
2. **photo_data**: Photo repository
3. **backend_logs**: Application logs

**List volumes**:
```bash
docker volume ls | grep photosort
```

**Inspect volume**:
```bash
docker volume inspect photosort_postgres_data
```

**Backup volume**:
```bash
# Database backup
docker-compose exec database pg_dump -U photosort_user PhotoSortData > backup_$(date +%Y%m%d).sql

# Photo data backup
docker run --rm -v photosort_photo_data:/data -v $(pwd):/backup alpine tar czf /backup/photos_backup.tar.gz -C /data .
```

**Restore volume**:
```bash
# Database restore
cat backup_20251205.sql | docker-compose exec -T database psql -U photosort_user PhotoSortData

# Photo data restore
docker run --rm -v photosort_photo_data:/data -v $(pwd):/backup alpine tar xzf /backup/photos_backup.tar.gz -C /data
```

**Remove volumes** (DELETES ALL DATA):
```bash
docker-compose down -v
```

---

## Networking

### Network Overview

Containers communicate via the `photosort-network` bridge network.

**Internal hostnames**:
- `database` → PostgreSQL (port 5432)
- `backend` → Spring Boot (port 8080)
- `frontend` → nginx (port 3000)

**External access**:
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- Database: `localhost:5432`

### Inspect Network

```bash
# List networks
docker network ls

# Inspect PhotoSort network
docker network inspect photosort_photosort-network

# See connected containers
docker network inspect photosort_photosort-network | grep Name
```

### Custom Ports

Change ports in `.env`:
```bash
FRONTEND_PORT=8000
BACKEND_PORT=8888
DB_PORT=5555
```

Then restart:
```bash
docker-compose down
docker-compose up -d
```

---

## Troubleshooting

### Container Won't Start

**Check logs**:
```bash
docker-compose logs backend
docker-compose logs frontend
docker-compose logs database
```

**Common issues**:

1. **Port already in use**:
   ```bash
   # Find process using port
   lsof -i :8080

   # Kill process or change port in .env
   ```

2. **Permission denied**:
   ```bash
   # Fix Docker permissions
   sudo usermod -aG docker $USER
   # Log out and back in
   ```

3. **Database connection fails**:
   ```bash
   # Check database health
   docker-compose exec database pg_isready

   # Verify credentials in .env match docker-compose.yml
   ```

### Backend Fails to Connect to Database

**Symptoms**: Backend logs show connection errors

**Solution**:
```bash
# 1. Verify database is healthy
docker-compose ps

# 2. Check database connection from backend
docker-compose exec backend wget -O- database:5432

# 3. Restart backend after database is ready
docker-compose restart backend
```

### Frontend Can't Reach Backend

**Symptoms**: API calls fail with network errors

**Solution**:
```bash
# 1. Check REACT_APP_API_BASE_URL in .env
# Should be http://localhost:8080 for local deployment

# 2. Rebuild frontend with correct URL
docker-compose build --build-arg REACT_APP_API_BASE_URL=http://localhost:8080 frontend
docker-compose up -d frontend
```

### Out of Disk Space

```bash
# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Remove everything (CAREFUL!)
docker system prune -a --volumes
```

### Health Check Failures

```bash
# View health check output
docker inspect --format='{{json .State.Health}}' photosort-backend | jq

# Manually test health endpoint
curl http://localhost:8080/actuator/health
curl http://localhost:3000/health
```

---

## Production Deployment

### Pre-Deployment Checklist

- [ ] Strong database password in `.env`
- [ ] OAuth credentials configured
- [ ] `JPA_DDL_AUTO=validate` (not `update`)
- [ ] `JPA_SHOW_SQL=false`
- [ ] HTTPS configured (reverse proxy)
- [ ] Firewall rules configured
- [ ] Backup strategy in place
- [ ] Monitoring configured
- [ ] Resource limits set

### Recommended Production Setup

**Use a reverse proxy (nginx/Traefik)**:
```nginx
server {
    listen 443 ssl;
    server_name photosort.example.com;

    ssl_certificate /etc/ssl/certs/photosort.crt;
    ssl_certificate_key /etc/ssl/private/photosort.key;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**Update OAuth redirect URIs** to use production domain:
- JavaScript origins: `https://photosort.example.com`
- Redirect URIs: `https://photosort.example.com/login/oauth2/code/google`

**Update `.env` for production**:
```bash
REACT_APP_API_BASE_URL=https://photosort.example.com
OAUTH_REDIRECT_URI=https://photosort.example.com/login/oauth2/code/google
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
JAVA_OPTS=-Xmx1G -Xms512m
```

### Automated Backups

**Cron job for daily backups**:
```bash
# Add to crontab (crontab -e)
0 2 * * * cd /path/to/PhotoSort-V1 && docker-compose exec -T database pg_dump -U photosort_user PhotoSortData | gzip > /backups/photosort_$(date +\%Y\%m\%d).sql.gz
```

### Monitoring

**View resource usage**:
```bash
docker stats
```

**Set up health check monitoring** (with Uptime Kuma, etc.):
- Backend: `http://localhost:8080/actuator/health`
- Frontend: `http://localhost:3000/health`

---

## Docker Compose Reference

### Full docker-compose.yml Structure

```yaml
services:
  database:   # PostgreSQL 15
  backend:    # Spring Boot (Java 17)
  frontend:   # React + nginx

volumes:
  postgres_data:  # Database files
  photo_data:     # Photo repository
  backend_logs:   # Application logs

networks:
  photosort-network:  # Bridge network
```

### Useful Commands Summary

```bash
# Build
docker-compose build
./scripts/build-docker.sh

# Start
docker-compose up -d
./scripts/start.sh

# Stop
docker-compose down
./scripts/stop.sh

# Logs
docker-compose logs -f
docker-compose logs -f backend

# Status
docker-compose ps

# Restart
docker-compose restart

# Shell access
docker-compose exec backend sh
docker-compose exec database psql -U photosort_user -d PhotoSortData

# Rebuild specific service
docker-compose build --no-cache backend
docker-compose up -d backend
```

---

## Additional Resources

- [BUILD_AND_DEPLOY.md](BUILD_AND_DEPLOY.md) - Non-Docker deployment options
- [SECRETS_SETUP.md](SECRETS_SETUP.md) - Credential management
- [PhotoSortUserDocumentation.md](docs/PhotoSortUserDocumentation.md) - User guide
- [PhotoSortDevDocumentation.md](docs/PhotoSortDevDocumentation.md) - Developer guide

---

## Support

For issues or questions:
1. Check logs: `docker-compose logs -f`
2. Review troubleshooting section above
3. Verify `.env` configuration
4. Check health endpoints
5. Consult BUILD_AND_DEPLOY.md for general deployment guidance
