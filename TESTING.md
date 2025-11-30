# PhotoSort Testing Guide

This guide walks you through testing the PhotoSort application components that have been implemented so far.

## Prerequisites

### Required Software
- **PostgreSQL 13+**: Database server
- **Java 21** (already installed) or Java 17+
- **Maven 3.8+**: Build tool (needs to be installed)
- **Git**: Version control (already installed)

### Environment Variables
You need to set these before running the application:

```bash
# Database credentials
export DB_USERNAME=your_postgres_username
export DB_PASSWORD=your_postgres_password

# Google OAuth credentials (get from Google Cloud Console)
export OAUTH_CLIENT_ID=your_google_client_id
export OAUTH_CLIENT_SECRET=your_google_client_secret
```

## Setup Instructions

### 1. Install Maven

**Ubuntu/Debian**:
```bash
sudo apt update
sudo apt install maven
mvn --version
```

**Verify installation**:
```bash
mvn --version
# Should show Maven 3.x.x
```

### 2. Set Up PostgreSQL Database

**Create databases**:
```bash
# Connect to PostgreSQL
sudo -u postgres psql

# In PostgreSQL prompt:
CREATE DATABASE "PhotoSortData";
CREATE DATABASE "PhotoSortDataTest";

# Create user if needed:
CREATE USER your_username WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO your_username;
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortDataTest" TO your_username;

# Exit PostgreSQL
\q
```

**Run schema script**:
```bash
cd /home/dms/Documents/development/photoSort/PhotoSort-V1/PhotoSortServices/src/main/resources

# Run on main database
psql -U $DB_USERNAME -d PhotoSortData -f schema.sql

# Verify tables created
psql -U $DB_USERNAME -d PhotoSortData -c "\dt"
```

### 3. Set Up Google OAuth (Optional for initial testing)

To test OAuth, you need Google OAuth credentials:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Go to Credentials → Create Credentials → OAuth 2.0 Client ID
5. Application type: Web application
6. Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
7. Copy Client ID and Client Secret
8. Set environment variables as shown above

**Skip OAuth for now**: If you want to test without OAuth, we can temporarily disable it.

## Testing Steps

### Test 1: Build the Project

```bash
cd /home/dms/Documents/development/photoSort/PhotoSort-V1/PhotoSortServices

# Clean build (skip tests for now)
mvn clean install -DskipTests

# Expected output:
# [INFO] BUILD SUCCESS
```

**What this tests**:
- All code compiles correctly
- Dependencies resolve properly
- Maven configuration is correct

### Test 2: Run Unit Tests

```bash
cd /home/dms/Documents/development/photoSort/PhotoSort-V1/PhotoSortServices

# Run all tests
mvn test

# Or run specific test class
mvn test -Dtest=DatabaseSchemaTest
mvn test -Dtest=DatabaseConnectionConfigTest
mvn test -Dtest=UserServiceTest
```

**Expected results**:
- ✅ DatabaseSchemaTest: 20+ tests pass
- ✅ DatabaseConnectionConfigTest: 8 tests pass
- ✅ UserServiceTest: 10 tests pass
- **Total**: 38+ tests should pass

**What this tests**:
- Database connectivity
- Entity mappings
- CRUD operations
- Foreign key constraints
- Cascade deletes
- Transaction management
- User authentication logic

### Test 3: Start the Application (Without OAuth)

If you don't have OAuth configured yet, let's temporarily disable it:

**Option A: With OAuth disabled** (for testing)

Create a temporary test profile:

```bash
# Create test properties without OAuth requirement
cat > src/main/resources/application-noauth.properties << 'EOF'
# Include main properties
spring.config.import=application.properties

# Disable security for testing
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
EOF

# Run with noauth profile
mvn spring-boot:run -Dspring-boot.run.profiles=noauth
```

**Option B: With OAuth configured**

```bash
# Make sure environment variables are set
echo $OAUTH_CLIENT_ID
echo $OAUTH_CLIENT_SECRET

# Run normally
mvn spring-boot:run
```

**Expected output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started PhotoSortApplication in X.XXX seconds
```

Application should start on http://localhost:8080

### Test 4: Manual API Testing

Once the application is running, test the endpoints:

**Test database connection** (in another terminal):
```bash
# Check application health
curl http://localhost:8080/actuator/health 2>/dev/null || echo "Health endpoint not configured"

# Test authentication status (without OAuth, this might return 401)
curl http://localhost:8080/api/auth/status
```

**Expected responses**:
- Without auth: 401 Unauthorized (this is correct!)
- With auth: User information returned

### Test 5: Database Verification

Verify the database has correct structure:

```bash
# Connect to database
psql -U $DB_USERNAME -d PhotoSortData

# List all tables
\dt

# Check users table structure
\d users

# Check photos table structure
\d photos

# Exit
\q
```

**Expected tables**:
- users
- photos
- exif_data
- metadata_fields
- photo_metadata
- tags
- photo_tags
- photo_permissions
- user_column_preferences
- scripts
- script_execution_log

## Troubleshooting

### Issue: Maven not found
```bash
sudo apt install maven
```

### Issue: PostgreSQL connection refused
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Start if needed
sudo systemctl start postgresql
```

### Issue: Database authentication failed
```bash
# Verify environment variables are set
echo $DB_USERNAME
echo $DB_PASSWORD

# Try connecting manually
psql -U $DB_USERNAME -d PhotoSortData
```

### Issue: Tests fail with "database does not exist"
```bash
# Create test database
sudo -u postgres psql -c 'CREATE DATABASE "PhotoSortDataTest";'
```

### Issue: OAuth errors on startup
**Solution 1**: Use the `-Dspring-boot.run.profiles=noauth` option to skip OAuth for now

**Solution 2**: Set dummy OAuth values temporarily:
```bash
export OAUTH_CLIENT_ID=dummy
export OAUTH_CLIENT_SECRET=dummy
```

### Issue: Port 8080 already in use
```bash
# Find what's using port 8080
sudo lsof -i :8080

# Kill the process or change port in application.properties
```

## Test Results Checklist

After running all tests, you should have:

- [ ] Maven build succeeds
- [ ] All 38+ unit tests pass
- [ ] Application starts without errors
- [ ] Database has all 11 tables
- [ ] Can connect to database
- [ ] (Optional) OAuth login works if configured

## Next Steps After Testing

If all tests pass:
1. ✅ Core infrastructure is working
2. ✅ Database layer is solid
3. ✅ Authentication foundation is ready
4. 🚀 Ready to implement frontend (Step 5)

If tests fail:
1. Review error messages carefully
2. Check environment variables
3. Verify PostgreSQL is running
4. Check logs in console output
5. Refer to troubleshooting section above

## Quick Start Command Sequence

Here's the minimal command sequence to test everything:

```bash
# 1. Set environment variables
export DB_USERNAME=your_postgres_username
export DB_PASSWORD=your_postgres_password

# 2. Create databases
sudo -u postgres psql -c 'CREATE DATABASE "PhotoSortData";'
sudo -u postgres psql -c 'CREATE DATABASE "PhotoSortDataTest";'

# 3. Run schema
cd /home/dms/Documents/development/photoSort/PhotoSort-V1/PhotoSortServices
psql -U $DB_USERNAME -d PhotoSortData -f src/main/resources/schema.sql

# 4. Build and test
mvn clean install

# 5. Run tests
mvn test

# 6. Start application (without OAuth for now)
mvn spring-boot:run -Dspring-boot.run.profiles=noauth
```

## Success Criteria

Your PhotoSort backend is working correctly if:
- ✅ Maven build completes successfully
- ✅ All 38+ tests pass
- ✅ Application starts and listens on port 8080
- ✅ Database has all required tables
- ✅ No errors in application logs

Good luck with testing! 🚀
