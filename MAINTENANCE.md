# PhotoSort Maintenance Guide

**Copyright 2025, David Snyderman**

## Table of Contents
1. [Regular Maintenance Tasks](#regular-maintenance-tasks)
2. [Backup Procedures](#backup-procedures)
3. [Update Procedures](#update-procedures)
4. [Monitoring](#monitoring)
5. [Log Management](#log-management)
6. [Database Maintenance](#database-maintenance)
7. [Security Updates](#security-updates)
8. [Disaster Recovery](#disaster-recovery)

---

## Regular Maintenance Tasks

### Daily Tasks

**1. Check Application Health**:
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend health
curl http://localhost:3000/health

# Database connectivity
psql -U photosort_user -d PhotoSortData -c "SELECT 1;"

# Docker (if using)
docker-compose ps
```

**2. Monitor Disk Space**:
```bash
# Check disk usage
df -h

# Check database size
psql -U photosort_user -d PhotoSortData -c "SELECT pg_size_pretty(pg_database_size('PhotoSortData'));"

# Check photo storage
du -sh /data/photos  # Or your configured photo path

# Docker volumes
docker system df
```

**3. Review Logs for Errors**:
```bash
# Backend errors (last 24 hours)
grep -i error PhotoSortServices/logs/photosort.log | tail -50

# Database errors
sudo tail -100 /var/log/postgresql/postgresql-13-main.log | grep ERROR

# Docker logs
docker-compose logs --since 24h | grep -i error
```

### Weekly Tasks

**1. Database Backup** (See [Backup Procedures](#backup-procedures)):
```bash
# Automated backup script
./scripts/backup-database.sh

# Verify backup
ls -lh /backups/photosort/
```

**2. Check Database Statistics**:
```sql
-- Connect to database
psql -U photosort_user -d PhotoSortData

-- Check table sizes
SELECT schemaname, tablename,
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check dead tuples (should be low)
SELECT schemaname, tablename, n_dead_tup
FROM pg_stat_user_tables
WHERE n_dead_tup > 1000
ORDER BY n_dead_tup DESC;
```

**3. Review User Activity**:
```sql
-- Active users
SELECT COUNT(DISTINCT user_id) as active_users
FROM users
WHERE last_login > NOW() - INTERVAL '7 days';

-- Photo uploads
SELECT COUNT(*) as photos_added
FROM photos
WHERE created_at > NOW() - INTERVAL '7 days';

-- Script executions
SELECT COUNT(*) as script_runs
FROM script_execution_log
WHERE started_at > NOW() - INTERVAL '7 days';
```

**4. Clean Up Old Logs**:
```bash
# Rotate application logs
find PhotoSortServices/logs/ -name "*.log" -mtime +30 -delete

# Compress old logs
find PhotoSortServices/logs/ -name "*.log" -mtime +7 -exec gzip {} \;

# Database log rotation (configure in postgresql.conf)
```

### Monthly Tasks

**1. Update Dependencies**:
```bash
# Check for Maven dependency updates
cd PhotoSortServices
mvn versions:display-dependency-updates

# Check for npm dependency updates
cd photosort-frontend
npm outdated

# Update (carefully, test after)
mvn versions:use-latest-releases
npm update
```

**2. Database Vacuum and Analyze**:
```sql
-- Full vacuum (requires downtime)
VACUUM FULL ANALYZE;

-- Or regular vacuum (no downtime)
VACUUM ANALYZE;

-- Reindex if needed
REINDEX DATABASE "PhotoSortData";
```

**3. Review and Archive Old Data**:
```sql
-- Check script execution log size
SELECT COUNT(*) FROM script_execution_log;

-- Archive old executions (older than 90 days)
BEGIN;
COPY (
    SELECT * FROM script_execution_log
    WHERE started_at < NOW() - INTERVAL '90 days'
) TO '/tmp/script_log_archive.csv' CSV HEADER;

DELETE FROM script_execution_log
WHERE started_at < NOW() - INTERVAL '90 days';
COMMIT;
```

**4. Security Audit**:
```bash
# Check for known vulnerabilities
cd PhotoSortServices
mvn dependency-check:check

cd photosort-frontend
npm audit

# Fix vulnerabilities
npm audit fix
```

### Quarterly Tasks

**1. Review and Optimize Database Indexes**:
```sql
-- Find unused indexes
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND schemaname = 'public';

-- Find missing indexes (tables with many sequential scans)
SELECT schemaname, tablename, seq_scan, seq_tup_read, seq_tup_read / seq_scan as avg_rows
FROM pg_stat_user_tables
WHERE seq_scan > 0
  AND schemaname = 'public'
ORDER BY seq_tup_read DESC;
```

**2. Performance Testing**:
```bash
# Load testing with Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/photos

# Monitor during load
top
iotop
```

**3. Disaster Recovery Test**:
```bash
# Test backup restore procedure
# See Disaster Recovery section below
```

---

## Backup Procedures

### Automated Database Backup

**Create backup script** (`/usr/local/bin/photosort-backup.sh`):
```bash
#!/bin/bash
# PhotoSort Database Backup Script
# Copyright 2025, David Snyderman

BACKUP_DIR="/backups/photosort"
DATE=$(date +%Y%m%d_%H%M%S)
KEEP_DAYS=30

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup database
pg_dump -U photosort_user PhotoSortData | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# Backup photos (if using local storage)
tar czf $BACKUP_DIR/photos_$DATE.tar.gz /data/photos

# Remove old backups
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +$KEEP_DAYS -delete
find $BACKUP_DIR -name "photos_*.tar.gz" -mtime +$KEEP_DAYS -delete

# Log completion
echo "$(date): Backup completed successfully" >> $BACKUP_DIR/backup.log
```

**Make executable and schedule**:
```bash
chmod +x /usr/local/bin/photosort-backup.sh

# Add to crontab (daily at 2 AM)
crontab -e
# Add:
0 2 * * * /usr/local/bin/photosort-backup.sh
```

### Docker Backup

```bash
# Database backup
docker-compose exec -T database pg_dump -U photosort_user PhotoSortData | gzip > backup_$(date +%Y%m%d).sql.gz

# Volume backup
docker run --rm -v photosort_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_$(date +%Y%m%d).tar.gz -C /data .
docker run --rm -v photosort_photo_data:/data -v $(pwd):/backup alpine tar czf /backup/photos_$(date +%Y%m%d).tar.gz -C /data .
```

### Verify Backup Integrity

```bash
# Test database backup
gunzip -c backup_20251205.sql.gz | head -50

# Check size (should not be empty)
ls -lh backup_*.sql.gz

# Test restore to temporary database
createdb test_restore
gunzip -c backup_20251205.sql.gz | psql -U photosort_user test_restore
dropdb test_restore
```

### Off-site Backup (AWS S3)

```bash
# Install AWS CLI
sudo apt-get install -y awscli
aws configure

# Upload to S3
aws s3 cp /backups/photosort/ s3://your-backup-bucket/photosort/ --recursive

# Automated S3 backup script
#!/bin/bash
/usr/local/bin/photosort-backup.sh
aws s3 sync /backups/photosort/ s3://your-backup-bucket/photosort/
```

---

## Update Procedures

### Update Application Code

**1. Backup First**:
```bash
./scripts/backup-database.sh
git rev-parse HEAD > /tmp/last-working-commit.txt
```

**2. Pull Latest Code**:
```bash
git pull origin master
```

**3. Update Backend**:
```bash
cd PhotoSortServices

# Clean build
mvn clean install

# Run tests
mvn test

# If tests pass, restart
pkill -f spring-boot
mvn spring-boot:run &
```

**4. Update Frontend**:
```bash
cd photosort-frontend

# Install new dependencies
npm install

# Run tests
npm test -- --coverage --watchAll=false

# Build
npm run build

# Restart
pkill -f react-scripts
npm start &
```

**5. Verify**:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:3000
```

**6. Rollback if Needed**:
```bash
git reset --hard $(cat /tmp/last-working-commit.txt)
# Rebuild and restart
```

### Update Docker Images

```bash
# Pull latest code
git pull origin master

# Rebuild images
docker-compose build --no-cache

# Stop old containers
docker-compose down

# Start new containers
docker-compose up -d

# Verify
docker-compose ps
docker-compose logs -f
```

### Update Dependencies

**Maven (Backend)**:
```bash
cd PhotoSortServices

# Check for updates
mvn versions:display-dependency-updates

# Update versions in pom.xml manually
# Or use:
mvn versions:use-latest-releases

# Test after update
mvn clean test
```

**npm (Frontend)**:
```bash
cd photosort-frontend

# Check for updates
npm outdated

# Update (minor versions)
npm update

# Update major versions manually in package.json
# Then:
npm install

# Test after update
npm test -- --coverage --watchAll=false
```

---

## Monitoring

### Application Monitoring

**Health Endpoints**:
```bash
# Backend
watch -n 60 'curl -s http://localhost:8080/actuator/health | jq'

# Frontend
watch -n 60 'curl -s http://localhost:3000/health'
```

**Resource Usage**:
```bash
# CPU and Memory
top -p $(pgrep -f spring-boot)
top -p $(pgrep -f react-scripts)

# Database connections
psql -U photosort_user -d PhotoSortData -c "SELECT count(*) FROM pg_stat_activity WHERE datname = 'PhotoSortData';"

# Disk I/O
iotop -p $(pgrep -f spring-boot)
```

### Docker Monitoring

```bash
# Container stats
docker stats

# Logs
docker-compose logs -f --tail=100

# Health status
docker-compose ps

# Resource usage
docker system df
```

### Set Up Alerts

**Using systemd (local)**:
```bash
# Create monitoring service
sudo nano /etc/systemd/system/photosort-monitor.service
```

```ini
[Unit]
Description=PhotoSort Monitor

[Service]
Type=oneshot
ExecStart=/usr/local/bin/photosort-monitor.sh

[Install]
WantedBy=multi-user.target
```

**Monitor script** (`/usr/local/bin/photosort-monitor.sh`):
```bash
#!/bin/bash
# Check if backend is responding
if ! curl -sf http://localhost:8080/actuator/health > /dev/null; then
    echo "PhotoSort backend is down!" | mail -s "PhotoSort Alert" admin@example.com
fi

# Check disk space
DISK_USAGE=$(df -h / | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $DISK_USAGE -gt 90 ]; then
    echo "Disk usage is ${DISK_USAGE}%" | mail -s "PhotoSort Disk Alert" admin@example.com
fi
```

---

## Log Management

### Configure Log Rotation

**Backend Logs** (`/etc/logrotate.d/photosort`):
```
/path/to/PhotoSort-V1/PhotoSortServices/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 photosort photosort
    sharedscripts
    postrotate
        systemctl reload photosort-backend
    endscript
}
```

**Database Logs** (postgresql.conf):
```
logging_collector = on
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d.log'
log_rotation_age = 1d
log_rotation_size = 100MB
```

### Centralized Logging (Optional)

**Using ELK Stack**:
```bash
# Install Filebeat
curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-8.11.0-amd64.deb
sudo dpkg -i filebeat-8.11.0-amd64.deb

# Configure
sudo nano /etc/filebeat/filebeat.yml
```

```yaml
filebeat.inputs:
- type: log
  paths:
    - /path/to/PhotoSort-V1/PhotoSortServices/logs/*.log
  fields:
    app: photosort
    env: production

output.elasticsearch:
  hosts: ["localhost:9200"]
```

---

## Database Maintenance

### Regular Maintenance

**Weekly Vacuum**:
```sql
-- Connect to database
psql -U photosort_user -d PhotoSortData

-- Vacuum all tables
VACUUM ANALYZE;
```

**Monthly Full Vacuum** (requires downtime):
```bash
# Stop application
systemctl stop photosort-backend
# Or: docker-compose stop backend

# Run vacuum
psql -U photosort_user -d PhotoSortData -c "VACUUM FULL ANALYZE;"

# Start application
systemctl start photosort-backend
# Or: docker-compose start backend
```

### Reindex Database

```sql
-- Check for bloated indexes
SELECT schemaname, tablename, indexname, pg_size_pretty(pg_relation_size(indexrelid))
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY pg_relation_size(indexrelid) DESC;

-- Reindex specific table
REINDEX TABLE photos;

-- Reindex entire database (requires downtime)
REINDEX DATABASE "PhotoSortData";
```

### Update Statistics

```sql
-- Update statistics for query planner
ANALYZE;

-- Update for specific table
ANALYZE photos;
```

---

## Security Updates

### Operating System Updates

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get dist-upgrade -y

# Reboot if kernel updated
sudo reboot
```

### PostgreSQL Security Updates

```bash
# Check current version
psql --version

# Update
sudo apt-get update
sudo apt-get install postgresql

# Restart
sudo systemctl restart postgresql
```

### Java Security Updates

```bash
# Check current version
java -version

# Update
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Verify
java -version
```

### Rotate Credentials

**OAuth Credentials**:
1. Create new OAuth client in Google Cloud Console
2. Update `application.properties`
3. Delete old OAuth client
4. Restart backend

**Database Password**:
```sql
-- In PostgreSQL
ALTER USER photosort_user WITH PASSWORD 'new_secure_password';
```

Update `application.properties` and restart.

---

## Disaster Recovery

### Database Failure

**Symptoms**: Database won't start, data corruption

**Recovery**:
```bash
# 1. Stop application
systemctl stop photosort-backend

# 2. Drop corrupted database
sudo -u postgres psql
DROP DATABASE "PhotoSortData";
CREATE DATABASE "PhotoSortData";
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;
\q

# 3. Restore from backup
gunzip -c /backups/photosort/db_20251205.sql.gz | psql -U photosort_user PhotoSortData

# 4. Verify
psql -U photosort_user -d PhotoSortData -c "SELECT COUNT(*) FROM photos;"

# 5. Start application
systemctl start photosort-backend
```

### Application Failure

**Symptoms**: Backend won't start after update

**Recovery**:
```bash
# 1. Rollback to last working commit
git log --oneline -n 10
git reset --hard abc123f  # Last working commit

# 2. Rebuild
cd PhotoSortServices
mvn clean install

# 3. Restart
systemctl restart photosort-backend
```

### Complete System Failure

**Prerequisites**: Regular backups to off-site location

**Recovery Steps**:
```bash
# 1. Set up new server (see BUILD_AND_DEPLOY.md)

# 2. Install dependencies
# ... (Java, PostgreSQL, etc.)

# 3. Clone repository
git clone https://github.com/davesnyd/PhotoSort-V1.git
cd PhotoSort-V1

# 4. Restore database backup
createdb PhotoSortData
gunzip -c backup_latest.sql.gz | psql PhotoSortData

# 5. Restore photos
tar xzf photos_latest.tar.gz -C /data/photos

# 6. Configure and start
cp application.properties.template PhotoSortServices/src/main/resources/application.properties
# Edit with credentials
mvn clean install
mvn spring-boot:run
```

### Test Recovery Procedure

**Quarterly Test**:
```bash
# 1. Create test environment
# 2. Restore latest backup
# 3. Verify application functions
# 4. Document any issues
# 5. Update recovery procedures
```

---

## Quick Reference

### Essential Commands

```bash
# Restart everything
systemctl restart photosort-backend postgresql
cd photosort-frontend && pkill -f react-scripts && npm start &

# Check status
systemctl status photosort-backend postgresql
curl http://localhost:8080/actuator/health

# Backup
pg_dump -U photosort_user PhotoSortData | gzip > backup_$(date +%Y%m%d).sql.gz

# Restore
gunzip -c backup.sql.gz | psql -U photosort_user PhotoSortData

# Logs
tail -f PhotoSortServices/logs/photosort.log
sudo tail -f /var/log/postgresql/postgresql-13-main.log

# Database maintenance
psql -U photosort_user -d PhotoSortData -c "VACUUM ANALYZE;"
```

### Maintenance Checklist

- [ ] Daily: Check health endpoints
- [ ] Daily: Review logs for errors
- [ ] Daily: Monitor disk space
- [ ] Weekly: Database backup
- [ ] Weekly: Review database statistics
- [ ] Monthly: Update dependencies
- [ ] Monthly: Database vacuum
- [ ] Monthly: Security audit
- [ ] Quarterly: Performance testing
- [ ] Quarterly: Disaster recovery test
