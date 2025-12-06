# PhotoSort Docker Images - Technical Reference

**Copyright 2025, David Snyderman**

## Table of Contents
1. [Image Overview](#image-overview)
2. [Backend Image (photosort-backend)](#backend-image-photosort-backend)
3. [Frontend Image (photosort-frontend)](#frontend-image-photosort-frontend)
4. [Database Image (postgres)](#database-image-postgres)
5. [Volume Configuration](#volume-configuration)
6. [Network Configuration](#network-configuration)
7. [Starting and Stopping Containers](#starting-and-stopping-containers)
8. [Clean Machine Deployment](#clean-machine-deployment)
9. [AWS Deployment](#aws-deployment)

---

## Image Overview

PhotoSort uses three Docker images to create a complete full-stack application:

| Image | Base Image | Size | Purpose | Ports |
|-------|-----------|------|---------|-------|
| photosort-backend | eclipse-temurin:17-jre-alpine | ~300MB | Spring Boot REST API | 8080 |
| photosort-frontend | nginx:1.25-alpine | ~50MB | React UI (nginx) | 3000 |
| postgres:15-alpine | postgres:15-alpine | ~240MB | PostgreSQL database | 5432 |

**Total stack size**: ~590MB

---

## Backend Image (photosort-backend)

### What's Inside

**Base Image**: `eclipse-temurin:17-jre-alpine`
- Alpine Linux (minimal footprint)
- Java 17 JRE (Eclipse Temurin distribution)
- Total size: ~180MB base + ~120MB application

**Application Components**:
```
/app/
├── photosort-backend.jar    # Spring Boot executable JAR (~80MB)
├── logs/                     # Application logs directory
└── (runtime files created by Spring Boot)
```

**Build Process** (Multi-stage):
1. **Stage 1 - Builder** (`maven:3.9-eclipse-temurin-17`):
   - Downloads Maven dependencies
   - Compiles Java source code
   - Runs tests (if not skipped)
   - Packages into JAR file

2. **Stage 2 - Runtime** (`eclipse-temurin:17-jre-alpine`):
   - Copies only the JAR file (no source, no build tools)
   - Creates non-root user `photosort`
   - Sets up log directory
   - Configures health check

### Configuration

**Environment Variables**:
```bash
# Database connection
SPRING_DATASOURCE_URL=jdbc:postgresql://database:5432/PhotoSortData
SPRING_DATASOURCE_USERNAME=photosort_user
SPRING_DATASOURCE_PASSWORD=<from .env>

# OAuth 2.0
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID=<from .env>
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET=<from .env>

# Git repository (optional)
GIT_REPO_PATH=/data/photos
GIT_REPO_URL=<from .env>
GIT_USERNAME=<from .env>
GIT_TOKEN=<from .env>

# Java options
JAVA_OPTS=-Xmx512m -Xms256m
SPRING_PROFILES_ACTIVE=prod
```

**Port Exposed**: `8080`

**Health Check**:
- Endpoint: `http://localhost:8080/actuator/health`
- Interval: 30 seconds
- Timeout: 10 seconds
- Start period: 60 seconds (allows app to start)
- Retries: 3

**User**: Runs as `photosort` (non-root, UID varies)

### External Storage

**Volumes Mounted**:
1. `photo_data:/data/photos` - Photo repository and Git clone location
2. `backend_logs:/app/logs` - Application log files

### Start/Stop Commands

**Start alone**:
```bash
docker-compose up -d backend
```

**Stop**:
```bash
docker-compose stop backend
```

**Restart**:
```bash
docker-compose restart backend
```

**View logs**:
```bash
docker-compose logs -f backend
```

**Shell access**:
```bash
docker-compose exec backend sh
```

### Build Details

**Dockerfile location**: `PhotoSortServices/Dockerfile`

**Build command**:
```bash
cd PhotoSortServices
docker build -t photosort-backend:latest .
```

**Build arguments**: None (uses environment variables at runtime)

---

## Frontend Image (photosort-frontend)

### What's Inside

**Base Image**: `nginx:1.25-alpine`
- Alpine Linux (minimal footprint)
- nginx 1.25 web server
- Total size: ~25MB base + ~25MB React build

**Application Components**:
```
/usr/share/nginx/html/
├── index.html                # React entry point
├── static/
│   ├── js/                   # React JavaScript bundles
│   ├── css/                  # Compiled CSS
│   └── media/                # Images, fonts
├── favicon.ico
├── manifest.json
└── (other React build artifacts)

/etc/nginx/conf.d/
└── default.conf              # Custom nginx configuration
```

**Build Process** (Multi-stage):
1. **Stage 1 - Builder** (`node:18-alpine`):
   - Installs npm dependencies
   - Builds React production bundle
   - Optimizes and minifies assets

2. **Stage 2 - Runtime** (`nginx:1.25-alpine`):
   - Copies only the built static files
   - Copies custom nginx configuration
   - Sets up non-root nginx user
   - Configures health check

### Configuration

**Build Arguments**:
```bash
REACT_APP_API_BASE_URL=http://localhost:8080  # Backend API URL
```

**nginx Configuration** (`photosort-frontend/nginx.conf`):
```nginx
server {
    listen 3000;
    server_name localhost;

    # Gzip compression enabled
    # Static asset caching (1 year)
    # Security headers (X-Frame-Options, etc.)
    # React Router support (all routes → index.html)
}
```

**Port Exposed**: `3000`

**Health Check**:
- Endpoint: `http://localhost:3000/health`
- Interval: 30 seconds
- Timeout: 3 seconds
- Start period: 10 seconds
- Retries: 3

**User**: Runs as `nginx` (non-root, UID 101)

### External Storage

**None** - Frontend is stateless, serves static files from image

### Start/Stop Commands

**Start alone**:
```bash
docker-compose up -d frontend
```

**Stop**:
```bash
docker-compose stop frontend
```

**Restart**:
```bash
docker-compose restart frontend
```

**View logs**:
```bash
docker-compose logs -f frontend
```

**Shell access**:
```bash
docker-compose exec frontend sh
```

### Build Details

**Dockerfile location**: `photosort-frontend/Dockerfile`

**Build command**:
```bash
cd photosort-frontend
docker build --build-arg REACT_APP_API_BASE_URL=http://localhost:8080 -t photosort-frontend:latest .
```

**Build arguments**:
- `REACT_APP_API_BASE_URL`: Backend API URL (baked into build)

**Important**: If you change the backend URL, you must rebuild the frontend image.

---

## Database Image (postgres)

### What's Inside

**Base Image**: `postgres:15-alpine`
- Alpine Linux
- PostgreSQL 15.x server
- Total size: ~240MB

**Database Files**:
```
/var/lib/postgresql/data/pgdata/
├── base/                     # Database files
├── global/                   # Global tables
├── pg_wal/                   # Write-ahead logs
├── pg_tblspc/                # Tablespaces
└── (other PostgreSQL system files)
```

**Initialization**:
On first startup, runs `/docker-entrypoint-initdb.d/init-db.sql`:
```sql
-- Grant privileges to photosort_user
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO photosort_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO photosort_user;
```

### Configuration

**Environment Variables**:
```bash
POSTGRES_DB=PhotoSortData           # Database name
POSTGRES_USER=photosort_user        # Database user
POSTGRES_PASSWORD=<from .env>       # User password
PGDATA=/var/lib/postgresql/data/pgdata  # Data directory
```

**Port Exposed**: `5432`

**Health Check**:
- Command: `pg_isready -U photosort_user -d PhotoSortData`
- Interval: 10 seconds
- Timeout: 5 seconds
- Retries: 5

**User**: Runs as `postgres` (UID 70)

### External Storage

**Volumes Mounted**:
1. `postgres_data:/var/lib/postgresql/data` - All database files
2. `./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql:ro` - Initialization script (read-only)

**Important**: The `postgres_data` volume contains ALL your data. Back it up regularly!

### Start/Stop Commands

**Start alone**:
```bash
docker-compose up -d database
```

**Stop**:
```bash
docker-compose stop database
```

**Restart**:
```bash
docker-compose restart database
```

**View logs**:
```bash
docker-compose logs -f database
```

**Database shell**:
```bash
docker-compose exec database psql -U photosort_user -d PhotoSortData
```

**Backup database**:
```bash
docker-compose exec database pg_dump -U photosort_user PhotoSortData > backup_$(date +%Y%m%d).sql
```

**Restore database**:
```bash
cat backup_20251205.sql | docker-compose exec -T database psql -U photosort_user PhotoSortData
```

### Build Details

**No custom build** - Uses official PostgreSQL image from Docker Hub.

**Pull command**:
```bash
docker pull postgres:15-alpine
```

---

## Volume Configuration

PhotoSort uses Docker **named volumes** for persistent data storage.

### Volume Details

| Volume Name | Container Path | Purpose | Size | Backup Priority |
|------------|----------------|---------|------|-----------------|
| photosort_postgres_data | /var/lib/postgresql/data | Database files | Varies | **CRITICAL** |
| photosort_photo_data | /data/photos | Photo repository | Varies | **HIGH** |
| photosort_backend_logs | /app/logs | Application logs | <1GB | LOW |

### Volume Management

**List volumes**:
```bash
docker volume ls | grep photosort
```

**Inspect volume**:
```bash
docker volume inspect photosort_postgres_data
```

**Volume location on host**:
```bash
docker volume inspect photosort_postgres_data | grep Mountpoint
# Typically: /var/lib/docker/volumes/photosort_postgres_data/_data
```

**Backup volumes**:
```bash
# Database (via pg_dump - RECOMMENDED)
docker-compose exec database pg_dump -U photosort_user PhotoSortData | gzip > db_backup_$(date +%Y%m%d).sql.gz

# Photo data (direct volume backup)
docker run --rm -v photosort_photo_data:/data -v $(pwd):/backup alpine tar czf /backup/photos_$(date +%Y%m%d).tar.gz -C /data .

# Logs (direct volume backup)
docker run --rm -v photosort_backend_logs:/data -v $(pwd):/backup alpine tar czf /backup/logs_$(date +%Y%m%d).tar.gz -C /data .
```

**Restore volumes**:
```bash
# Database
gunzip < db_backup_20251205.sql.gz | docker-compose exec -T database psql -U photosort_user PhotoSortData

# Photo data
docker run --rm -v photosort_photo_data:/data -v $(pwd):/backup alpine tar xzf /backup/photos_20251205.tar.gz -C /data
```

**Delete volumes** (DESTRUCTIVE):
```bash
# Stop containers first
docker-compose down

# Delete all data (CAUTION!)
docker-compose down -v
```

---

## Network Configuration

### Network Details

**Network Name**: `photosort_photosort-network`

**Network Type**: Bridge (default Docker network type)

**Subnet**: Automatically assigned by Docker (usually 172.x.x.x)

### Container Hostnames

Within the Docker network, containers can reach each other using these hostnames:

| Container | Internal Hostname | Port |
|-----------|------------------|------|
| Database | `database` | 5432 |
| Backend | `backend` | 8080 |
| Frontend | `frontend` | 3000 |

**Example**: Backend connects to database using:
```
jdbc:postgresql://database:5432/PhotoSortData
```

### External Access

From the host machine:

| Service | URL | Maps To |
|---------|-----|---------|
| Frontend | http://localhost:3000 | Container port 3000 |
| Backend | http://localhost:8080 | Container port 8080 |
| Database | localhost:5432 | Container port 5432 |

### Port Mapping

Defined in `docker-compose.yml`:
```yaml
frontend:
  ports:
    - "${FRONTEND_PORT:-3000}:3000"  # Host:Container

backend:
  ports:
    - "${BACKEND_PORT:-8080}:8080"

database:
  ports:
    - "${DB_PORT:-5432}:5432"
```

Change ports in `.env` file:
```bash
FRONTEND_PORT=8000
BACKEND_PORT=8888
DB_PORT=5433
```

---

## Starting and Stopping Containers

### Full Stack Operations

**Start all services**:
```bash
# Using script
./scripts/start.sh

# Using docker-compose
docker-compose up -d

# With build (if images changed)
docker-compose up -d --build
```

**Stop all services**:
```bash
# Using script
./scripts/stop.sh

# Using docker-compose
docker-compose down

# Stop and remove volumes (DELETES DATA!)
docker-compose down -v
```

**Restart all services**:
```bash
docker-compose restart
```

**View status**:
```bash
docker-compose ps
```

### Individual Container Operations

**Start specific container**:
```bash
docker-compose up -d database
docker-compose up -d backend
docker-compose up -d frontend
```

**Stop specific container**:
```bash
docker-compose stop database
docker-compose stop backend
docker-compose stop frontend
```

**Restart specific container**:
```bash
docker-compose restart database
docker-compose restart backend
docker-compose restart frontend
```

**Remove specific container**:
```bash
docker-compose rm -f database
docker-compose rm -f backend
docker-compose rm -f frontend
```

### Advanced Operations

**Start with logs visible**:
```bash
docker-compose up
```

**Rebuild and start**:
```bash
docker-compose up -d --build --force-recreate
```

**Scale containers** (not applicable for PhotoSort, but shown for reference):
```bash
docker-compose up -d --scale backend=3
```

**Stop without removing containers**:
```bash
docker-compose stop
```

**Start stopped containers**:
```bash
docker-compose start
```

---

## Clean Machine Deployment

### Prerequisites

Install on clean Ubuntu/Debian machine:
```bash
# Update system
sudo apt-get update
sudo apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# Log out and back in for group to take effect

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version
```

### Deployment Steps

**1. Clone repository**:
```bash
git clone https://github.com/davesnyd/PhotoSort-V1.git
cd PhotoSort-V1
```

**2. Configure environment**:
```bash
# Copy template
cp .env.docker.template .env

# Edit configuration
nano .env
```

Fill in required values:
```bash
DB_PASSWORD=your_secure_password
OAUTH_CLIENT_ID=your_google_client_id
OAUTH_CLIENT_SECRET=your_google_client_secret
```

**3. Build images**:
```bash
./scripts/build-docker.sh

# Or manually
docker-compose build
```

**4. Deploy**:
```bash
./scripts/deploy-docker.sh

# Or manually
docker-compose up -d
```

**5. Verify deployment**:
```bash
# Check container status
docker-compose ps

# Check logs
docker-compose logs -f

# Test endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:3000/health

# Access in browser
# http://localhost:3000
```

**6. Configure firewall** (if needed):
```bash
# Allow HTTP traffic
sudo ufw allow 3000/tcp
sudo ufw allow 8080/tcp

# Or use reverse proxy (recommended for production)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

### Post-Deployment

**Set up automatic startup**:
```bash
# Create systemd service
sudo nano /etc/systemd/system/photosort.service
```

```ini
[Unit]
Description=PhotoSort Docker Compose
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/path/to/PhotoSort-V1
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start
sudo systemctl daemon-reload
sudo systemctl enable photosort
sudo systemctl start photosort
```

**Set up backups**:
```bash
# Create backup script
nano /usr/local/bin/photosort-backup.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/backups/photosort"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

cd /path/to/PhotoSort-V1
docker-compose exec -T database pg_dump -U photosort_user PhotoSortData | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# Rotate backups (keep 7 days)
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +7 -delete
```

```bash
# Make executable
chmod +x /usr/local/bin/photosort-backup.sh

# Add to cron
crontab -e
# Add: 0 2 * * * /usr/local/bin/photosort-backup.sh
```

---

## AWS Deployment

### Option 1: EC2 Deployment (Recommended for Small/Medium)

**1. Launch EC2 Instance**:
- AMI: Ubuntu Server 22.04 LTS
- Instance type: t3.medium or larger (2 vCPU, 4 GB RAM minimum)
- Storage: 30 GB+ root volume
- Security Group:
  - SSH (22) - Your IP only
  - HTTP (80) - 0.0.0.0/0
  - HTTPS (443) - 0.0.0.0/0
  - Custom TCP (3000, 8080) - Optional for testing

**2. Connect and install Docker**:
```bash
ssh -i your-key.pem ubuntu@ec2-xx-xx-xx-xx.compute.amazonaws.com

# Install Docker and Docker Compose
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker ubuntu
exit
# Reconnect to apply group membership
```

**3. Deploy PhotoSort**:
```bash
# Clone repository
git clone https://github.com/davesnyd/PhotoSort-V1.git
cd PhotoSort-V1

# Configure
cp .env.docker.template .env
nano .env  # Update with production values
```

Update `.env` for production:
```bash
REACT_APP_API_BASE_URL=https://your-domain.com
OAUTH_REDIRECT_URI=https://your-domain.com/login/oauth2/code/google
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
```

**4. Set up reverse proxy (nginx)**:
```bash
# Install nginx on host
sudo apt-get install -y nginx certbot python3-certbot-nginx

# Configure nginx
sudo nano /etc/nginx/sites-available/photosort
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /login/oauth2/ {
        proxy_pass http://localhost:8080/login/oauth2/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/photosort /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx

# Get SSL certificate
sudo certbot --nginx -d your-domain.com
```

**5. Deploy containers**:
```bash
cd ~/PhotoSort-V1
./scripts/deploy-docker.sh
```

**6. Configure automatic backups to S3**:
```bash
# Install AWS CLI
sudo apt-get install -y awscli

# Configure AWS credentials
aws configure

# Create backup script
nano /usr/local/bin/photosort-s3-backup.sh
```

```bash
#!/bin/bash
BUCKET="s3://your-backup-bucket/photosort"
DATE=$(date +%Y%m%d_%H%M%S)
TEMP_DIR="/tmp/photosort-backup-$DATE"

mkdir -p $TEMP_DIR

# Backup database
cd /home/ubuntu/PhotoSort-V1
docker-compose exec -T database pg_dump -U photosort_user PhotoSortData | gzip > $TEMP_DIR/db_$DATE.sql.gz

# Upload to S3
aws s3 cp $TEMP_DIR/db_$DATE.sql.gz $BUCKET/

# Cleanup
rm -rf $TEMP_DIR

# Lifecycle policy on S3 bucket handles retention
```

```bash
chmod +x /usr/local/bin/photosort-s3-backup.sh

# Add to cron
crontab -e
# Add: 0 2 * * * /usr/local/bin/photosort-s3-backup.sh
```

### Option 2: ECS Deployment (Recommended for Production/Scale)

**1. Create ECR repositories**:
```bash
# From your local machine with AWS CLI configured
aws ecr create-repository --repository-name photosort-backend
aws ecr create-repository --repository-name photosort-frontend
```

**2. Build and push images**:
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Tag and push backend
docker tag photosort-backend:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/photosort-backend:latest
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/photosort-backend:latest

# Tag and push frontend
docker tag photosort-frontend:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/photosort-frontend:latest
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/photosort-frontend:latest
```

**3. Create RDS PostgreSQL instance**:
- Engine: PostgreSQL 15
- Instance: db.t3.micro (or larger)
- Storage: 20 GB GP3
- Public access: No
- Security group: Allow 5432 from ECS security group

**4. Create ECS cluster**:
- Launch type: Fargate
- VPC: Default or custom
- Subnets: At least 2 in different AZs

**5. Create task definitions**:

**Backend task definition**:
```json
{
  "family": "photosort-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/photosort-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://your-rds-endpoint:5432/PhotoSortData"
        },
        {
          "name": "SPRING_DATASOURCE_USERNAME",
          "value": "photosort_user"
        }
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:photosort/db-password"
        },
        {
          "name": "SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:photosort/oauth-client-id"
        },
        {
          "name": "SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:photosort/oauth-client-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/photosort-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

**Frontend task definition**:
```json
{
  "family": "photosort-frontend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "frontend",
      "image": "YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/photosort-frontend:latest",
      "portMappings": [
        {
          "containerPort": 3000,
          "protocol": "tcp"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/photosort-frontend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

**6. Create ECS services**:
- Backend service: 1-3 tasks, target group for ALB
- Frontend service: 1-3 tasks, target group for ALB

**7. Create Application Load Balancer**:
- Scheme: Internet-facing
- Listeners:
  - HTTP (80) → Redirect to HTTPS
  - HTTPS (443) → Target groups
- Rules:
  - `/api/*` → Backend target group
  - `/login/oauth2/*` → Backend target group
  - `/*` → Frontend target group

**8. Configure auto-scaling**:
- Target tracking based on CPU/memory
- Min tasks: 1
- Max tasks: 10

### Option 3: ECS with Docker Compose (AWS Copilot)

**Simplest production deployment to AWS**:

```bash
# Install AWS Copilot
brew install aws/tap/copilot-cli  # macOS
# Or: https://aws.github.io/copilot-cli/

# Initialize application
copilot app init photosort

# Create environment
copilot env init --name production

# Create backend service
copilot svc init --name backend --svc-type "Load Balanced Web Service" --dockerfile PhotoSortServices/Dockerfile

# Create frontend service
copilot svc init --name frontend --svc-type "Load Balanced Web Service" --dockerfile photosort-frontend/Dockerfile

# Create database addon
copilot storage init

# Deploy
copilot deploy --env production
```

Copilot automatically:
- Creates VPC, subnets, security groups
- Provisions RDS database
- Builds and pushes Docker images to ECR
- Creates ECS cluster, services, tasks
- Sets up Application Load Balancer
- Configures auto-scaling
- Sets up CloudWatch logs

### Cost Estimates (AWS)

**EC2 Deployment**:
- t3.medium instance: ~$30/month
- 30 GB EBS storage: ~$3/month
- Data transfer: ~$5/month
- **Total**: ~$40/month

**ECS + Fargate + RDS**:
- Fargate tasks (2): ~$30/month
- RDS db.t3.micro: ~$15/month
- Application Load Balancer: ~$20/month
- Data transfer: ~$10/month
- **Total**: ~$75/month

---

## Additional Resources

- [DOCKER.md](DOCKER.md) - Comprehensive Docker guide
- [BUILD_AND_DEPLOY.md](BUILD_AND_DEPLOY.md) - Non-Docker deployment
- [docker-compose.yml](docker-compose.yml) - Service definitions
- [PhotoSortServices/Dockerfile](PhotoSortServices/Dockerfile) - Backend image
- [photosort-frontend/Dockerfile](photosort-frontend/Dockerfile) - Frontend image
