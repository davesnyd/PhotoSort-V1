# PhotoSort Troubleshooting Guide

**Copyright 2025, David Snyderman**

## Table of Contents
1. [Common Issues](#common-issues)
2. [Backend Issues](#backend-issues)
3. [Frontend Issues](#frontend-issues)
4. [Database Issues](#database-issues)
5. [Authentication Issues](#authentication-issues)
6. [Docker Issues](#docker-issues)
7. [Performance Issues](#performance-issues)
8. [Getting Help](#getting-help)

---

## Common Issues

### Application Won't Start

**Symptoms**: Backend or frontend fails to start

**Check these first**:
```bash
# 1. Check if ports are in use
lsof -i :8080  # Backend
lsof -i :3000  # Frontend
lsof -i :5432  # Database

# 2. Check if database is running
sudo systemctl status postgresql
# Or for Docker:
docker-compose ps database

# 3. Check Java version
java -version  # Should be 17+

# 4. Check Node version
node --version  # Should be 18+
```

**Solution**:
```bash
# Kill process on port
lsof -ti :8080 | xargs kill -9

# Start database
sudo systemctl start postgresql

# Verify Java/Node versions
sudo update-alternatives --config java
nvm use 18
```

### "Connection Refused" Errors

**Symptoms**: Frontend can't reach backend, or backend can't reach database

**Diagnosis**:
```bash
# Test backend from frontend
curl http://localhost:8080/actuator/health

# Test database from backend
psql -U photosort_user -d PhotoSortData -h localhost

# Check firewall
sudo ufw status
```

**Solution**:
```bash
# Ensure backend is running
cd PhotoSortServices
mvn spring-boot:run

# Ensure database is accessible
sudo ufw allow 5432/tcp

# Check application.properties has correct database URL
cat PhotoSortServices/src/main/resources/application.properties | grep datasource
```

### Changes Not Appearing

**Symptoms**: Code changes don't show up in running application

**Frontend**:
```bash
# Clear cache and rebuild
rm -rf node_modules package-lock.json
npm install
npm start

# Or hard refresh browser: Ctrl+Shift+R
```

**Backend**:
```bash
# Clean build
mvn clean install
mvn spring-boot:run
```

**Docker**:
```bash
# Rebuild images
docker-compose build --no-cache
docker-compose up -d --force-recreate
```

---

## Backend Issues

### Backend Fails to Start

**Error**: `Port 8080 is already in use`

**Solution**:
```bash
# Find and kill process
lsof -ti :8080 | xargs kill -9

# Or change port in application.properties
server.port=8081
```

**Error**: `Failed to configure a DataSource`

**Solution**:
```bash
# Check application.properties exists and has credentials
ls -la PhotoSortServices/src/main/resources/application.properties

# Verify database connection
psql -U photosort_user -d PhotoSortData

# Check environment variables
echo $DB_USERNAME
echo $DB_PASSWORD
```

**Error**: `ClassNotFoundException` or `NoSuchMethodError`

**Solution**:
```bash
# Clean Maven cache
mvn dependency:purge-local-repository
mvn clean install
```

### Backend Crashes During Runtime

**Error**: `OutOfMemoryError: Java heap space`

**Solution**:
```bash
# Increase heap size
export MAVEN_OPTS="-Xmx1024m"
mvn spring-boot:run

# Or in Docker (.env):
JAVA_OPTS=-Xmx1G -Xms512m
```

**Error**: `Too many open files`

**Solution**:
```bash
# Check current limit
ulimit -n

# Increase limit
ulimit -n 4096

# Make permanent (add to /etc/security/limits.conf):
* soft nofile 4096
* hard nofile 8192
```

### API Returns 500 Errors

**Diagnosis**:
```bash
# Check backend logs
tail -f PhotoSortServices/logs/photosort.log

# Or for Docker:
docker-compose logs -f backend

# Test endpoint directly
curl -v http://localhost:8080/api/photos
```

**Common causes**:
1. **Database connection lost**: Restart database
2. **Missing permissions**: Check PhotoPermission table
3. **Invalid data**: Check request payload format
4. **Exception in code**: Review stack trace in logs

---

## Frontend Issues

### Frontend Won't Build

**Error**: `npm ERR! code ELIFECYCLE`

**Solution**:
```bash
# Clean install
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

**Error**: `Module not found: Can't resolve 'xyz'`

**Solution**:
```bash
# Install missing dependency
npm install xyz

# Or reinstall all
rm -rf node_modules
npm install
```

### White Screen / Blank Page

**Diagnosis**:
```bash
# Check browser console (F12)
# Look for JavaScript errors

# Check if build succeeded
ls -la build/

# Verify React app is running
curl http://localhost:3000
```

**Solution**:
```bash
# Rebuild
npm run build

# Check for routing issues
# Ensure BrowserRouter is configured correctly

# Verify .env file
cat .env
# Should have:
REACT_APP_API_BASE_URL=http://localhost:8080
```

### API Calls Failing (CORS Errors)

**Error**: `Access to fetch at 'http://localhost:8080' blocked by CORS policy`

**Solution**:

Backend already has CORS configured, but verify:
```java
// PhotoSortServices/src/main/java/com/photoSort/config/SecurityConfig.java
// Should have:
.cors(cors -> cors.configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);
    return config;
}))
```

If still failing:
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Verify .env has correct API URL
echo $REACT_APP_API_BASE_URL
```

### React Router Not Working

**Symptoms**: 404 errors when refreshing page on non-root routes

**Solution for nginx**:
```nginx
# In nginx.conf, ensure:
location / {
    try_files $uri $uri/ /index.html;
}
```

**Solution for development**:
```bash
# This is handled automatically by react-scripts
npm start
```

---

## Database Issues

### Can't Connect to Database

**Error**: `FATAL: password authentication failed for user "photosort_user"`

**Solution**:
```bash
# Reset password
sudo -u postgres psql
ALTER USER photosort_user WITH PASSWORD 'new_password';
\q

# Update application.properties
nano PhotoSortServices/src/main/resources/application.properties
# Update spring.datasource.password
```

**Error**: `FATAL: database "PhotoSortData" does not exist`

**Solution**:
```bash
# Create database
sudo -u postgres psql
CREATE DATABASE "PhotoSortData";
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;
\q
```

**Error**: `Connection refused` or `could not connect to server`

**Solution**:
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Start if not running
sudo systemctl start postgresql

# Check if listening on correct port
sudo lsof -i :5432

# Check pg_hba.conf for authentication settings
sudo nano /etc/postgresql/13/main/pg_hba.conf
# Should have line like:
# local   all             all                                     md5
```

### Tables Don't Exist

**Error**: `relation "photos" does not exist`

**Solution**:
```bash
# Run schema.sql
cd PhotoSortServices/src/main/resources
psql -U photosort_user -d PhotoSortData -f schema.sql

# Or let JPA create tables (in application.properties):
spring.jpa.hibernate.ddl-auto=update
```

### Permission Denied Errors

**Error**: `permission denied for table photos`

**Solution**:
```bash
sudo -u postgres psql PhotoSortData
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO photosort_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO photosort_user;
\q
```

### Database is Slow

**Diagnosis**:
```sql
-- Check for missing indexes
SELECT schemaname, tablename, attname, n_distinct, correlation
FROM pg_stats
WHERE schemaname = 'public'
ORDER BY tablename, attname;

-- Check query performance
EXPLAIN ANALYZE SELECT * FROM photos WHERE owner_id = 1;

-- Check for long-running queries
SELECT pid, now() - pg_stat_activity.query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active'
ORDER BY duration DESC;
```

**Solution**:
```sql
-- Add missing indexes (examples)
CREATE INDEX idx_photos_owner ON photos(owner_id);
CREATE INDEX idx_photo_tags_photo ON photo_tags(photo_id);

-- Vacuum and analyze
VACUUM ANALYZE;

-- Update statistics
ANALYZE;
```

---

## Authentication Issues

### OAuth Login Fails

**Error**: `redirect_uri_mismatch`

**Solution**:
1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Verify authorized redirect URIs include:
   - `http://localhost:8080/login/oauth2/code/google`
3. Verify authorized JavaScript origins include:
   - `http://localhost:8080`

**Error**: `invalid_client`

**Solution**:
```bash
# Verify OAuth credentials in application.properties
cat PhotoSortServices/src/main/resources/application.properties | grep oauth

# Ensure client ID and secret are correct
# Get new credentials from Google Cloud Console if needed
```

### User Not Created After Login

**Diagnosis**:
```bash
# Check backend logs for errors
tail -f PhotoSortServices/logs/photosort.log

# Check database for user
psql -U photosort_user -d PhotoSortData
SELECT * FROM users WHERE email = 'your@email.com';
\q
```

**Common causes**:
1. Database connection issue (check logs)
2. Email validation error (check user email format)
3. Transaction rollback (check for exceptions)

### Session Expires Immediately

**Solution**:
```properties
# In application.properties, increase session timeout
server.servlet.session.timeout=30m

# Ensure cookies are being set
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false  # true for HTTPS only
```

### "Access Denied" for Admin Features

**Diagnosis**:
```sql
-- Check user role
SELECT user_id, email, is_admin FROM users WHERE email = 'your@email.com';
```

**Solution**:
```sql
-- Promote user to admin
UPDATE users SET is_admin = true WHERE email = 'your@email.com';
```

---

## Docker Issues

### Docker Build Fails

**Error**: `manifest for eclipse-temurin:17-jre-alpine not found`

**Solution**:
```bash
# Pull base image manually
docker pull eclipse-temurin:17-jre-alpine

# Clean Docker cache
docker builder prune -a

# Rebuild
docker-compose build --no-cache
```

**Error**: `ERROR [internal] load metadata for ...`

**Solution**:
```bash
# Check Docker daemon is running
sudo systemctl status docker

# Restart Docker
sudo systemctl restart docker

# Check internet connectivity
curl -I https://hub.docker.com
```

### Container Won't Start

**Error**: `port is already allocated`

**Solution**:
```bash
# Find process using port
lsof -i :8080

# Change port in .env
BACKEND_PORT=8888

# Restart containers
docker-compose down
docker-compose up -d
```

**Error**: `exec: "sh": executable file not found`

**Solution**:
```bash
# Check Dockerfile ENTRYPOINT/CMD format
# Should use shell form or exec form consistently

# For alpine images, ensure sh is available:
RUN apk add --no-cache bash
```

### Container Keeps Restarting

**Diagnosis**:
```bash
# Check container logs
docker-compose logs -f backend

# Check container status
docker-compose ps

# Inspect container
docker inspect photosort-backend
```

**Common causes**:
1. **Application crash**: Check logs for exceptions
2. **Health check failing**: Test health endpoint manually
3. **Resource limits**: Increase memory/CPU limits
4. **Missing dependencies**: Rebuild image

### Can't Access Container Volumes

**Solution**:
```bash
# Find volume location
docker volume inspect photosort_postgres_data

# Access volume via container
docker run --rm -v photosort_postgres_data:/data alpine ls -la /data

# Copy files from volume
docker run --rm -v photosort_postgres_data:/data -v $(pwd):/backup alpine cp -r /data /backup
```

---

## Performance Issues

### Backend Slow Response Times

**Diagnosis**:
```bash
# Check backend CPU/memory usage
top -p $(pgrep -f spring-boot)

# Profile slow endpoints
curl -w "@curl-format.txt" http://localhost:8080/api/photos

# Where curl-format.txt contains:
# time_total: %{time_total}s
```

**Solutions**:

1. **Enable query logging**:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

2. **Add database indexes** (see Database Issues above)

3. **Enable connection pooling** (already configured):
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

4. **Enable caching**:
```java
// Add @EnableCaching to main application class
// Add @Cacheable to expensive service methods
```

### Frontend Slow Loading

**Solutions**:

1. **Enable production build**:
```bash
npm run build
# Serve build/ directory instead of development server
```

2. **Optimize images**:
```bash
# Use WebP format
# Implement lazy loading
# Add thumbnails for large images
```

3. **Enable compression in nginx**:
```nginx
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css application/json application/javascript;
```

4. **Implement pagination**:
```javascript
// Already implemented in PhotoTable component
// Ensure page size is reasonable (10-50 items)
```

### Database Query Performance

**Solutions**:

1. **Analyze slow queries**:
```sql
-- Enable query logging in postgresql.conf
log_min_duration_statement = 1000  # Log queries taking >1 second

-- Check logs
tail -f /var/log/postgresql/postgresql-13-main.log
```

2. **Optimize N+1 queries**:
```java
// Use @EntityGraph or JOIN FETCH
@Query("SELECT p FROM Photo p JOIN FETCH p.owner WHERE p.photoId = :id")
Photo findByIdWithOwner(@Param("id") Long id);
```

3. **Add missing indexes**:
```sql
-- Identify missing indexes
SELECT schemaname, tablename, attname
FROM pg_stats
WHERE schemaname = 'public' AND n_distinct > 100
  AND attname NOT IN (
    SELECT column_name FROM information_schema.constraint_column_usage
  );
```

---

## Getting Help

### Collect Diagnostic Information

Before asking for help, collect:

**1. Version Information**:
```bash
java -version
node --version
npm --version
mvn --version
psql --version
docker --version
docker-compose --version
```

**2. Application Logs**:
```bash
# Backend logs
tail -100 PhotoSortServices/logs/photosort.log

# Frontend errors (browser console)
# Open DevTools (F12) → Console tab

# Database logs
sudo tail -100 /var/log/postgresql/postgresql-13-main.log

# Docker logs
docker-compose logs --tail=100
```

**3. Configuration**:
```bash
# Backend config (REMOVE PASSWORDS!)
cat PhotoSortServices/src/main/resources/application.properties | grep -v password

# Frontend config
cat photosort-frontend/.env

# Docker config
cat .env | grep -v PASSWORD
```

**4. System Information**:
```bash
uname -a
df -h
free -h
```

### Check Existing Documentation

- [README.md](README.md) - Getting started
- [BUILD_AND_DEPLOY.md](BUILD_AND_DEPLOY.md) - Deployment guide
- [DOCKER.md](DOCKER.md) - Docker deployment
- [DOCKER_IMAGES.md](DOCKER_IMAGES.md) - Docker technical reference
- [SECRETS_SETUP.md](SECRETS_SETUP.md) - Credential management
- [docs/PhotoSortUserDocumentation.md](docs/PhotoSortUserDocumentation.md) - User guide
- [docs/PhotoSortDevDocumentation.md](docs/PhotoSortDevDocumentation.md) - Developer guide

### Report Issues

When reporting issues, include:
1. Description of what you were trying to do
2. Expected behavior
3. Actual behavior (with error messages)
4. Steps to reproduce
5. Diagnostic information (from above)
6. Screenshots if applicable

---

## Quick Reference

### Restart Everything

**Local Development**:
```bash
# Stop all
lsof -ti :8080 | xargs kill -9  # Backend
lsof -ti :3000 | xargs kill -9  # Frontend
sudo systemctl restart postgresql  # Database

# Start all
cd PhotoSortServices && mvn spring-boot:run &
cd photosort-frontend && npm start &
```

**Docker**:
```bash
docker-compose restart
# Or full rebuild:
docker-compose down
docker-compose up -d --build
```

### Clear All Caches

```bash
# Maven
mvn clean
rm -rf ~/.m2/repository/com/photoSort

# npm
rm -rf photosort-frontend/node_modules
rm -rf photosort-frontend/package-lock.json
npm cache clean --force

# Docker
docker system prune -a
docker volume prune
```

### Reset Database

**WARNING: This deletes all data!**

```bash
# Backup first!
pg_dump -U photosort_user PhotoSortData > backup_$(date +%Y%m%d).sql

# Drop and recreate
sudo -u postgres psql
DROP DATABASE "PhotoSortData";
CREATE DATABASE "PhotoSortData";
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;
\q

# Restore schema
cd PhotoSortServices/src/main/resources
psql -U photosort_user -d PhotoSortData -f schema.sql
```
