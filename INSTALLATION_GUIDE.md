# PhotoSort Installation Guide (Non-Containerized)

**Copyright 2025, David Snyderman**

This guide covers installing PhotoSort on a fresh Ubuntu 22.04/24.04 LTS server without Docker.

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [System Preparation](#2-system-preparation)
3. [Install Required Software](#3-install-required-software)
4. [Database Setup](#4-database-setup)
5. [Application Setup](#5-application-setup)
6. [Backend Configuration](#6-backend-configuration)
7. [Frontend Configuration](#7-frontend-configuration)
8. [Build the Application](#8-build-the-application)
9. [Configure Nginx Reverse Proxy](#9-configure-nginx-reverse-proxy)
10. [Set Up Systemd Services](#10-set-up-systemd-services)
11. [Google OAuth Configuration](#11-google-oauth-configuration)
12. [Photo Repository Setup](#12-photo-repository-setup)
13. [First Run and Verification](#13-first-run-and-verification)
14. [SSL/HTTPS Setup (Optional)](#14-sslhttps-setup-optional)
15. [Troubleshooting](#15-troubleshooting)

---

## 1. Prerequisites

### Hardware Requirements
- **CPU**: 2+ cores recommended
- **RAM**: 4GB minimum, 8GB recommended
- **Disk**: 20GB for application + space for photos
- **Network**: Internet access for OAuth and package installation

### Operating System
- Ubuntu 22.04 LTS or 24.04 LTS (recommended)
- Debian 11/12 (also supported)
- Other Linux distributions may work with package manager adjustments

### Access Requirements
- Root or sudo access
- SSH access to the server
- Domain name (optional, for production with SSL)

---

## 2. System Preparation

### Update System Packages

```bash
sudo apt update
sudo apt upgrade -y
```

### Set Timezone

```bash
sudo timedatectl set-timezone America/New_York  # Adjust to your timezone
```

### Create Application User (Optional but Recommended)

```bash
sudo useradd -m -s /bin/bash photosort
sudo usermod -aG sudo photosort
```

### Create Application Directories

```bash
sudo mkdir -p /opt/photosort
sudo mkdir -p /var/log/photosort
sudo mkdir -p /data/photos
sudo chown -R $USER:$USER /opt/photosort
sudo chown -R $USER:$USER /var/log/photosort
sudo chown -R $USER:$USER /data/photos
```

---

## 3. Install Required Software

### 3.1 Install Java 17

```bash
# Install OpenJDK 17
sudo apt install -y openjdk-17-jdk

# Verify installation
java -version
# Should show: openjdk version "17.x.x"

# Set JAVA_HOME (add to ~/.bashrc or /etc/environment)
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc
```

### 3.2 Install Maven

```bash
sudo apt install -y maven

# Verify installation
mvn -version
# Should show: Apache Maven 3.8.x or higher
```

### 3.3 Install Node.js 18+

```bash
# Install Node.js using NodeSource repository
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node --version  # Should show v18.x.x or higher
npm --version   # Should show 9.x.x or higher
```

### 3.4 Install PostgreSQL 14+

```bash
# Install PostgreSQL
sudo apt install -y postgresql postgresql-contrib

# Start and enable PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Verify installation
psql --version
# Should show: psql (PostgreSQL) 14.x or higher
```

### 3.5 Install Nginx

```bash
sudo apt install -y nginx

# Start and enable Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# Verify installation
nginx -v
```

### 3.6 Install Git

```bash
sudo apt install -y git

# Verify installation
git --version
```

### 3.7 Install Python 3 (for STAG AI Tagging - Optional)

```bash
sudo apt install -y python3 python3-pip python3-venv

# Verify installation
python3 --version
```

### 3.8 Install Additional Utilities

```bash
sudo apt install -y curl wget unzip htop
```

---

## 4. Database Setup

### 4.1 Create Database User and Database

```bash
# Switch to postgres user
sudo -u postgres psql

# In PostgreSQL shell, run:
CREATE USER photosort_user WITH PASSWORD 'your_secure_password_here';
CREATE DATABASE "PhotoSortData" OWNER photosort_user;
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;

# For the user to create tables
\c "PhotoSortData"
GRANT ALL ON SCHEMA public TO photosort_user;

# Exit PostgreSQL
\q
```

### 4.2 Configure PostgreSQL Authentication

```bash
# Edit pg_hba.conf to allow password authentication
sudo nano /etc/postgresql/14/main/pg_hba.conf
```

Find the line:
```
local   all             all                                     peer
```

Change it to:
```
local   all             all                                     md5
```

Also add (for TCP/IP connections):
```
host    all             all             127.0.0.1/32            md5
```

Restart PostgreSQL:
```bash
sudo systemctl restart postgresql
```

### 4.3 Test Database Connection

```bash
psql -U photosort_user -d PhotoSortData -h localhost
# Enter the password when prompted
# You should get a PostgreSQL prompt

# Exit with:
\q
```

---

## 5. Application Setup

### 5.1 Clone the Repository

```bash
cd /opt/photosort
git clone https://github.com/your-username/PhotoSort-V1.git .

# Or if copying from local machine:
# scp -r /path/to/PhotoSort-V1/* user@server:/opt/photosort/
```

### 5.2 Verify Directory Structure

```bash
ls -la /opt/photosort
# Should see:
# - PhotoSortServices/  (backend)
# - photosort-frontend/ (frontend)
# - docs/
# - docker-compose.yml
# - etc.
```

---

## 6. Backend Configuration

### 6.1 Create Application Properties

```bash
cd /opt/photosort/PhotoSortServices/src/main/resources

# Copy template
cp application.properties.template application.properties

# Edit configuration
nano application.properties
```

### 6.2 Configure application.properties

Replace the contents with your configuration:

```properties
# Copyright 2025, David Snyderman

# Application name
spring.application.name=PhotoSortServices

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/PhotoSortData
spring.datasource.username=photosort_user
spring.datasource.password=your_secure_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Connection Pool Configuration
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Logging
logging.level.org.hibernate.SQL=WARN
logging.level.com.photoSort=INFO
logging.file.name=/var/log/photosort/backend.log

# Server Configuration
server.port=8080

# Trust forwarded headers from nginx proxy
server.forward-headers-strategy=native

# OAuth 2.0 Configuration (Google)
# Get these from: https://console.cloud.google.com/apis/credentials
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://YOUR_DOMAIN_OR_IP:3000/login/oauth2/code/google

# Frontend URL for redirects after OAuth
app.frontend.url=http://YOUR_DOMAIN_OR_IP:3000

# Session Configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false

# Git Configuration (Photo Repository)
git.repo.path=/data/photos
git.repo.url=
git.username=
git.token=
git.poll.interval.minutes=5

# STAG Configuration (AI Tagging - Optional)
stag.script.path=/opt/photosort/stag-main/stag.py
stag.python.executable=python3
```

### 6.3 Set Secure File Permissions

```bash
chmod 600 /opt/photosort/PhotoSortServices/src/main/resources/application.properties
```

---

## 7. Frontend Configuration

### 7.1 Create Environment File

```bash
cd /opt/photosort/photosort-frontend

# Create .env file
nano .env
```

Add the following content:

```env
REACT_APP_API_BASE_URL=http://YOUR_DOMAIN_OR_IP:3000
```

**Note**: When using Nginx as a reverse proxy, the frontend and API will be served from the same origin (port 3000), so the API base URL should match the frontend URL.

### 7.2 For Production Build

```bash
# Create production environment file
nano .env.production
```

```env
REACT_APP_API_BASE_URL=http://YOUR_DOMAIN_OR_IP:3000
```

---

## 8. Build the Application

### 8.1 Build Backend

```bash
cd /opt/photosort/PhotoSortServices

# Clean and build
mvn clean package -DskipTests

# Verify JAR was created
ls -la target/PhotoSortServices-*.jar
```

### 8.2 Build Frontend

```bash
cd /opt/photosort/photosort-frontend

# Install dependencies
npm install

# Build for production
npm run build

# Verify build was created
ls -la build/
```

---

## 9. Configure Nginx Reverse Proxy

### 9.1 Create Nginx Configuration

```bash
sudo nano /etc/nginx/sites-available/photosort
```

Add the following configuration:

```nginx
server {
    listen 3000;
    server_name YOUR_DOMAIN_OR_IP;

    # Frontend static files
    root /opt/photosort/photosort-frontend/build;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # Frontend routes - serve index.html for React Router
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy to backend
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $http_host;

        # WebSocket support (if needed)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # OAuth endpoints proxy
    location /oauth2/ {
        proxy_pass http://127.0.0.1:8080/oauth2/;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $http_host;
    }

    location /login/oauth2/ {
        proxy_pass http://127.0.0.1:8080/login/oauth2/;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $http_host;
    }

    # Logout endpoint
    location /logout {
        proxy_pass http://127.0.0.1:8080/logout;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $http_host;
    }

    # Photo thumbnails (if stored locally)
    location /thumbnails/ {
        alias /data/photos/thumbnails/;
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # Actuator health endpoint
    location /actuator/ {
        proxy_pass http://127.0.0.1:8080/actuator/;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 9.2 Enable the Site

```bash
# Remove default site
sudo rm -f /etc/nginx/sites-enabled/default

# Enable PhotoSort site
sudo ln -s /etc/nginx/sites-available/photosort /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

### 9.3 Configure Firewall (if enabled)

```bash
# Allow HTTP traffic on port 3000
sudo ufw allow 3000/tcp

# Or if using standard ports:
# sudo ufw allow 80/tcp
# sudo ufw allow 443/tcp
```

---

## 10. Set Up Systemd Services

### 10.1 Create Backend Service

```bash
sudo nano /etc/systemd/system/photosort-backend.service
```

Add the following content:

```ini
[Unit]
Description=PhotoSort Backend Service
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=root
Group=root
WorkingDirectory=/opt/photosort/PhotoSortServices

# Java options
Environment="JAVA_OPTS=-Xms512m -Xmx1024m"

# Run the JAR
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/photosort/PhotoSortServices/target/PhotoSortServices-0.0.1-SNAPSHOT.jar

# Restart policy
Restart=on-failure
RestartSec=10

# Logging
StandardOutput=append:/var/log/photosort/backend.log
StandardError=append:/var/log/photosort/backend-error.log

[Install]
WantedBy=multi-user.target
```

### 10.2 Enable and Start Services

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable services to start on boot
sudo systemctl enable photosort-backend

# Start backend service
sudo systemctl start photosort-backend

# Check status
sudo systemctl status photosort-backend
```

### 10.3 View Logs

```bash
# View backend logs
sudo journalctl -u photosort-backend -f

# Or view log files directly
tail -f /var/log/photosort/backend.log
```

---

## 11. Google OAuth Configuration

### 11.1 Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Navigate to **APIs & Services** → **Credentials**

### 11.2 Configure OAuth Consent Screen

1. Go to **APIs & Services** → **OAuth consent screen**
2. Choose **External** user type
3. Fill in required fields:
   - **App name**: PhotoSort
   - **User support email**: your email
   - **Developer contact**: your email
4. Add scopes:
   - `.../auth/userinfo.email`
   - `.../auth/userinfo.profile`
5. Add test users (your email) if in testing mode
6. Save and continue

### 11.3 Create OAuth Client ID

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth client ID**
3. Choose **Web application**
4. Configure:
   - **Name**: PhotoSort Web Client
   - **Authorized JavaScript origins**:
     - `http://YOUR_DOMAIN_OR_IP:3000`
   - **Authorized redirect URIs**:
     - `http://YOUR_DOMAIN_OR_IP:3000/login/oauth2/code/google`
5. Click **Create**
6. Copy the **Client ID** and **Client Secret**

### 11.4 Update Application Configuration

Edit `/opt/photosort/PhotoSortServices/src/main/resources/application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_ACTUAL_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_ACTUAL_CLIENT_SECRET
spring.security.oauth2.client.registration.google.redirect-uri=http://YOUR_DOMAIN_OR_IP:3000/login/oauth2/code/google
app.frontend.url=http://YOUR_DOMAIN_OR_IP:3000
```

Rebuild and restart:

```bash
cd /opt/photosort/PhotoSortServices
mvn clean package -DskipTests
sudo systemctl restart photosort-backend
```

---

## 12. Photo Repository Setup

### 12.1 Initialize Git Repository for Photos

PhotoSort monitors a Git repository for photo changes. You need to set up a Git repository in your photos directory:

```bash
# Navigate to photos directory
cd /data/photos

# Initialize Git repository
git init

# Configure Git user (required for commits)
git config user.email "photosort@localhost"
git config user.name "PhotoSort"

# Create initial commit
touch .gitkeep
git add .
git commit -m "Initial commit"
```

### 12.2 Add Photos to Repository

```bash
# Copy photos to the repository
cp -r /path/to/your/photos/* /data/photos/

# Add and commit
cd /data/photos
git add .
git commit -m "Add photos"
```

### 12.3 Verify Configuration

Ensure `application.properties` has the correct path:

```properties
git.repo.path=/data/photos
```

### 12.4 Trigger Initial Scan

After starting the application:
1. Log in as administrator
2. Go to **Configuration** page
3. Verify **Repository Path** is `/data/photos`
4. Click **Rescan Photos Now**

---

## 13. First Run and Verification

### 13.1 Start All Services

```bash
# Ensure PostgreSQL is running
sudo systemctl status postgresql

# Start backend
sudo systemctl start photosort-backend

# Check backend is running
sudo systemctl status photosort-backend

# Nginx should already be running
sudo systemctl status nginx
```

### 13.2 Verify Backend Health

```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### 13.3 Access the Application

1. Open browser and navigate to: `http://YOUR_DOMAIN_OR_IP:3000`
2. You should see the PhotoSort login page
3. Click **Sign in with Google**
4. Complete Google authentication
5. You should be redirected to the PhotoSort home page

### 13.4 Create Admin User

The first user to log in is a regular user. To make yourself an admin:

```bash
psql -U photosort_user -d PhotoSortData -h localhost

# In PostgreSQL:
UPDATE users SET user_type = 'ADMIN' WHERE email = 'your.email@gmail.com';
\q
```

Log out and log back in to get admin privileges.

### 13.5 Verify Photo Import

1. Go to **Configuration** (admin only)
2. Verify **Repository Path** is correct
3. Click **Rescan Photos Now**
4. Go to **Photos** page
5. You should see your imported photos

---

## 14. SSL/HTTPS Setup (Optional)

### 14.1 Install Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
```

### 14.2 Obtain SSL Certificate

```bash
sudo certbot --nginx -d your-domain.com
```

Follow the prompts to:
1. Enter your email
2. Agree to terms
3. Choose whether to redirect HTTP to HTTPS

### 14.3 Update Application Configuration

After enabling HTTPS, update `application.properties`:

```properties
spring.security.oauth2.client.registration.google.redirect-uri=https://your-domain.com/login/oauth2/code/google
app.frontend.url=https://your-domain.com
server.servlet.session.cookie.secure=true
```

Update Google OAuth Console:
1. Add `https://your-domain.com` to **Authorized JavaScript origins**
2. Add `https://your-domain.com/login/oauth2/code/google` to **Authorized redirect URIs**

Rebuild and restart the backend.

### 14.4 Auto-Renewal

Certbot sets up auto-renewal automatically. Verify:

```bash
sudo certbot renew --dry-run
```

---

## 15. Troubleshooting

### Backend Won't Start

**Check logs:**
```bash
sudo journalctl -u photosort-backend -n 100
tail -f /var/log/photosort/backend.log
```

**Common issues:**
- **Database connection failed**: Verify PostgreSQL is running and credentials are correct
- **Port already in use**: Check if another process is using port 8080
- **Java not found**: Verify JAVA_HOME is set correctly

### OAuth Login Fails

**"redirect_uri_mismatch" error:**
- Verify the redirect URI in Google Console matches exactly
- Check `application.properties` redirect-uri setting
- Ensure protocol (http/https) matches

**"Access blocked" error:**
- Add your email as a test user in Google Console
- Or publish the OAuth consent screen

### Photos Not Importing

**Check backend logs for errors:**
```bash
grep -i "git\|poll\|photo" /var/log/photosort/backend.log
```

**Verify repository:**
```bash
cd /data/photos
git status
ls -la
```

**Common issues:**
- Repository path doesn't exist
- Not a Git repository (missing .git folder)
- No commits in repository
- Permission issues (check file ownership)

### Nginx Errors

**Test configuration:**
```bash
sudo nginx -t
```

**Check logs:**
```bash
sudo tail -f /var/log/nginx/error.log
```

### Database Issues

**Connect to database:**
```bash
psql -U photosort_user -d PhotoSortData -h localhost
```

**Check tables exist:**
```sql
\dt
```

**Check users:**
```sql
SELECT user_id, email, user_type FROM users;
```

---

## Quick Reference

### Service Commands

```bash
# Backend
sudo systemctl start photosort-backend
sudo systemctl stop photosort-backend
sudo systemctl restart photosort-backend
sudo systemctl status photosort-backend

# Nginx
sudo systemctl reload nginx
sudo systemctl restart nginx

# PostgreSQL
sudo systemctl restart postgresql
```

### Log Locations

| Service | Log Location |
|---------|--------------|
| Backend | `/var/log/photosort/backend.log` |
| Backend (systemd) | `journalctl -u photosort-backend` |
| Nginx Access | `/var/log/nginx/access.log` |
| Nginx Error | `/var/log/nginx/error.log` |
| PostgreSQL | `/var/log/postgresql/` |

### Important Paths

| Path | Purpose |
|------|---------|
| `/opt/photosort/` | Application root |
| `/opt/photosort/PhotoSortServices/` | Backend code |
| `/opt/photosort/photosort-frontend/` | Frontend code |
| `/data/photos/` | Photo repository |
| `/var/log/photosort/` | Application logs |

### Rebuild After Changes

```bash
# Backend
cd /opt/photosort/PhotoSortServices
mvn clean package -DskipTests
sudo systemctl restart photosort-backend

# Frontend
cd /opt/photosort/photosort-frontend
npm run build
# No restart needed - Nginx serves static files
```

---

## Support

For issues and questions:
- Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues
- Review [docs/PhotoSortUserDocumentation.md](docs/PhotoSortUserDocumentation.md) for usage
- Review [docs/PhotoSortDevDocumentation.md](docs/PhotoSortDevDocumentation.md) for technical details
