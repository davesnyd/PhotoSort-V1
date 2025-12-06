# PhotoSort - Build and Deployment Guide

**Copyright 2025, David Snyderman**

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Build](#local-development-build)
3. [Production Build](#production-build)
4. [Configuration](#configuration)
5. [Deployment Options](#deployment-options)
6. [Docker Deployment](#docker-deployment)

---

## Prerequisites

### Required Software
- **Java**: JDK 17 or higher
- **Node.js**: 18.x or higher
- **Maven**: 3.8 or higher
- **PostgreSQL**: 13 or higher
- **Docker**: 20.10 or higher (for containerized deployment)
- **Docker Compose**: 1.29 or higher (for containerized deployment)
- **Git**: 2.x or higher

### Verify Installation
```bash
java -version        # Should show Java 17+
node --version       # Should show v18+
npm --version        # Should show 9+
mvn --version        # Should show 3.8+
psql --version       # Should show 13+
docker --version     # Should show 20.10+
docker-compose --version  # Should show 1.29+
```

---

## Local Development Build

### 1. Clone Repository
```bash
git clone https://github.com/davesnyd/PhotoSort-V1.git
cd PhotoSort-V1
```

### 2. Database Setup
```bash
# Create PostgreSQL database
sudo -u postgres psql

CREATE DATABASE "PhotoSortData";
CREATE USER photosort_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO photosort_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO photosort_user;
\q
```

### 3. Backend Configuration
```bash
cd PhotoSortServices/src/main/resources

# Copy template
cp application.properties.template application.properties

# Edit with your credentials
nano application.properties
```

Fill in:
- Database username/password
- OAuth client ID/secret (from Google Cloud Console)
- Git repository path and credentials (if using Git polling)

### 4. Backend Build
```bash
cd PhotoSortServices

# Clean and build
mvn clean install

# Run tests
mvn test

# Package JAR
mvn package
```

The JAR will be created at: `target/PhotoSortServices-0.0.1-SNAPSHOT.jar`

### 5. Frontend Configuration
```bash
cd photosort-frontend

# Create environment file
cat > .env << EOF
REACT_APP_API_BASE_URL=http://localhost:8080
EOF
```

### 6. Frontend Build
```bash
cd photosort-frontend

# Install dependencies
npm install

# Run tests
npm test -- --coverage --watchAll=false

# Build for production
npm run build
```

The build will be created in: `build/`

---

## Production Build

### Backend Production JAR
```bash
cd PhotoSortServices

# Build with production profile
mvn clean package -Pprod -DskipTests

# JAR location
ls -lh target/PhotoSortServices-0.0.1-SNAPSHOT.jar
```

### Frontend Production Build
```bash
cd photosort-frontend

# Set production API URL
cat > .env.production << EOF
REACT_APP_API_BASE_URL=https://your-production-domain.com
EOF

# Build
npm run build

# Build output
ls -lh build/
```

### Optimize Build
```bash
# Backend: Enable compression
# Add to application.properties:
# server.compression.enabled=true
# server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain

# Frontend: Already optimized with create-react-app production build
```

---

## Configuration

### Environment Variables (Production)

Create `.env` file for production:

```bash
# Database
export DB_USERNAME="photosort_user"
export DB_PASSWORD="your_secure_password"
export DB_URL="jdbc:postgresql://localhost:5432/PhotoSortData"

# OAuth
export OAUTH_CLIENT_ID="your_google_client_id"
export OAUTH_CLIENT_SECRET="your_google_client_secret"

# Git Repository
export GIT_REPO_PATH="/path/to/photo/repository"
export GIT_REPO_URL="https://github.com/username/photos.git"
export GIT_USERNAME="your_github_username"
export GIT_TOKEN="your_github_token"

# Server
export SERVER_PORT="8080"
export FRONTEND_URL="http://localhost:3000"
```

Load environment:
```bash
source .env
```

### Application Properties for Production

Update `application.properties` to use environment variables:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/PhotoSortData}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

spring.security.oauth2.client.registration.google.client-id=${OAUTH_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${OAUTH_CLIENT_SECRET}

git.repo.path=${GIT_REPO_PATH:/path/to/photos}
git.repo.url=${GIT_REPO_URL}
git.username=${GIT_USERNAME}
git.token=${GIT_TOKEN}

server.port=${SERVER_PORT:8080}
```

---

## Deployment Options

### Option 1: Standalone JAR Deployment

```bash
# Run backend
cd PhotoSortServices
java -jar target/PhotoSortServices-0.0.1-SNAPSHOT.jar

# Serve frontend with nginx or Apache
# See nginx configuration below
```

### Option 2: Systemd Service (Linux)

Create `/etc/systemd/system/photosort-backend.service`:

```ini
[Unit]
Description=PhotoSort Backend Service
After=postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=photosort
WorkingDirectory=/opt/photosort/backend
ExecStart=/usr/bin/java -jar /opt/photosort/backend/PhotoSortServices-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=photosort-backend

Environment="DB_USERNAME=photosort_user"
Environment="DB_PASSWORD=your_password"
Environment="OAUTH_CLIENT_ID=your_client_id"
Environment="OAUTH_CLIENT_SECRET=your_client_secret"

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable photosort-backend
sudo systemctl start photosort-backend
sudo systemctl status photosort-backend
```

### Option 3: Nginx Frontend Configuration

Create `/etc/nginx/sites-available/photosort`:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Frontend
    location / {
        root /var/www/photosort/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Backend API proxy
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # OAuth callback proxy
    location /login/oauth2/ {
        proxy_pass http://localhost:8080/login/oauth2/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable:
```bash
sudo ln -s /etc/nginx/sites-available/photosort /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## Docker Deployment

See [DOCKER.md](DOCKER.md) for comprehensive Docker deployment instructions.

Quick start:
```bash
# Build and run all containers
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all containers
docker-compose down
```

---

## Verification

### Check Backend
```bash
curl http://localhost:8080/api/health
```

### Check Frontend
```bash
curl http://localhost:3000
# Or visit in browser
```

### Check Database Connection
```bash
psql -U photosort_user -d PhotoSortData -c "SELECT COUNT(*) FROM photos;"
```

---

## Troubleshooting

### Backend won't start
```bash
# Check logs
tail -f logs/photosort-backend.log

# Check port availability
lsof -i :8080

# Verify database connectivity
psql -U photosort_user -d PhotoSortData
```

### Frontend build fails
```bash
# Clear cache and rebuild
rm -rf node_modules package-lock.json
npm install
npm run build
```

### Database connection issues
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check credentials
psql -U photosort_user -d PhotoSortData

# Check pg_hba.conf for authentication settings
sudo nano /etc/postgresql/13/main/pg_hba.conf
```

---

## Security Checklist

- [ ] Changed default database passwords
- [ ] OAuth credentials rotated and secured
- [ ] Application properties not committed to git
- [ ] HTTPS enabled for production
- [ ] Firewall configured to restrict access
- [ ] Database backups configured
- [ ] Log rotation configured
- [ ] Security updates applied

---

## Maintenance

### Backup Database
```bash
pg_dump -U photosort_user PhotoSortData > backup_$(date +%Y%m%d).sql
```

### Restore Database
```bash
psql -U photosort_user PhotoSortData < backup_20251205.sql
```

### Update Application
```bash
# Pull latest code
git pull origin master

# Rebuild backend
cd PhotoSortServices
mvn clean package

# Rebuild frontend
cd photosort-frontend
npm install
npm run build

# Restart services
sudo systemctl restart photosort-backend
sudo systemctl reload nginx
```

---

## Additional Resources

- [Docker Deployment Guide](DOCKER.md)
- [User Documentation](docs/PhotoSortUserDocumentation.md)
- [Developer Documentation](docs/PhotoSortDevDocumentation.md)
- [Security Setup](SECRETS_SETUP.md)
- [Credential Restoration](CREDENTIAL_RESTORATION_GUIDE.md)
