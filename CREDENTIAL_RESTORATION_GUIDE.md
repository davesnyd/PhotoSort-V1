# Credential Restoration & Rotation Guide

## ✅ Force Push Status: VERIFIED COMPLETE

**Status:** Your force push was successful!
- Local HEAD: `7f5f2fc1273841791bcdca2ff200e01eb75abacd`
- Remote HEAD: `7f5f2fc1273841791bcdca2ff200e01eb75abacd`
- ✓ Git history cleaned
- ✓ Secrets removed from all commits
- ✓ Remote repository updated

---

## 1️⃣ RESTORE POSTGRES CREDENTIALS

### Option A: Use Your Existing Postgres Password

If you know your current postgres password:

```bash
# Edit application.properties
nano PhotoSortServices/src/main/resources/application.properties
```

Find these lines and fill them in:
```properties
spring.datasource.username=postgres
spring.datasource.password=YOUR_ACTUAL_POSTGRES_PASSWORD
```

### Option B: Set/Reset Postgres Password

If you don't know the password or want to set a new one:

```bash
# 1. Connect to postgres as superuser
sudo -u postgres psql

# 2. Set password (replace 'postgres' and 'your_new_password')
ALTER USER postgres WITH PASSWORD 'your_new_password';

# 3. Verify user exists
\du

# 4. Exit psql
\q
```

Then update `application.properties`:
```properties
spring.datasource.username=postgres
spring.datasource.password=your_new_password
```

### Option C: Create a New Database User (Recommended for Production)

```bash
# 1. Connect as postgres superuser
sudo -u postgres psql

# 2. Create new user
CREATE USER photosort_user WITH PASSWORD 'strong_password_here';

# 3. Grant permissions on PhotoSortData database
GRANT ALL PRIVILEGES ON DATABASE "PhotoSortData" TO photosort_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO photosort_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO photosort_user;

# 4. Exit
\q
```

Then update `application.properties`:
```properties
spring.datasource.username=photosort_user
spring.datasource.password=strong_password_here
```

### Test Database Connection

```bash
# Test the credentials work
PGPASSWORD='your_password' psql -U postgres -d PhotoSortData -c "SELECT 1;"

# Or with your new user
PGPASSWORD='strong_password_here' psql -U photosort_user -d PhotoSortData -c "SELECT 1;"
```

---

## 2️⃣ ROTATE GOOGLE OAUTH CREDENTIALS

**⚠️ CRITICAL: You MUST do this because the old credentials were exposed!**

### Step-by-Step OAuth Rotation:

#### Step 1: Delete Old OAuth Client

1. Go to: https://console.cloud.google.com/apis/credentials
2. Log in with your Google account
3. Select your project (or create one if needed)
4. Find the **OAuth 2.0 Client ID** that was exposed
5. Click the **trash/delete icon** next to it
6. Confirm deletion

#### Step 2: Create New OAuth Client

1. Still on https://console.cloud.google.com/apis/credentials
2. Click **"+ CREATE CREDENTIALS"** at the top
3. Select **"OAuth client ID"**
4. If prompted, configure consent screen:
   - Click "CONFIGURE CONSENT SCREEN"
   - Choose "External" (unless internal to organization)
   - Fill in app name: "PhotoSort"
   - Add support email
   - Click "SAVE AND CONTINUE"
   - Add scopes: `email`, `profile`
   - Click "SAVE AND CONTINUE"
   - Add test users (your email)
   - Click "SAVE AND CONTINUE"

5. Back at "Create OAuth client ID":
   - Application type: **Web application**
   - Name: "PhotoSort Local Development"
   - Authorized JavaScript origins:
     - `http://localhost:8080`
   - Authorized redirect URIs:
     - `http://localhost:8080/login/oauth2/code/google`
   - Click **CREATE**

6. A dialog appears with your new credentials:
   - **Client ID**: Something like `123456-abcdefg.apps.googleusercontent.com`
   - **Client secret**: Something like `GOCSPX-abc123def456`
   - **COPY BOTH** - you won't see the secret again!

#### Step 3: Update application.properties

```bash
nano PhotoSortServices/src/main/resources/application.properties
```

Find and update these lines:
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_NEW_CLIENT_ID_HERE
spring.security.oauth2.client.registration.google.client-secret=YOUR_NEW_CLIENT_SECRET_HERE
```

Replace with the values from Step 2.

#### Step 4: Test OAuth Login

```bash
# Start the backend
cd PhotoSortServices
mvn spring-boot:run

# In another terminal, start frontend
cd photosort-frontend
npm start
```

1. Open browser: http://localhost:3000
2. Click "Login with Google"
3. Should redirect to Google OAuth consent
4. Authorize the app
5. Should redirect back and log you in

If you get errors:
- Check redirect URI matches exactly
- Check client ID/secret are correct
- Check Google consent screen is configured
- Check test users are added

---

## 3️⃣ ROTATE GIT PERSONAL ACCESS TOKEN (If Applicable)

If you had a GitHub token in `git.token`:

### Step 1: Revoke Old Token

1. Go to: https://github.com/settings/tokens
2. Find your old token (it was exposed)
3. Click **Delete** or **Revoke**
4. Confirm deletion

### Step 2: Create New Token

1. Still on https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Note: "PhotoSort Git Polling"
4. Expiration: Choose an expiration (90 days recommended)
5. Select scopes:
   - ✓ `repo` (Full control of private repositories)
   - Or just `public_repo` if your Photos repo is public
6. Click **"Generate token"**
7. **COPY THE TOKEN** - you won't see it again!
   - Format: `ghp_abc123def456...`

### Step 3: Update application.properties

```bash
nano PhotoSortServices/src/main/resources/application.properties
```

Find and update:
```properties
git.token=ghp_YOUR_NEW_TOKEN_HERE
git.username=davesnyd
git.repo.url=https://github.com/davesnyd/PhotoSort-Photos.git
```

---

## 4️⃣ VERIFY EVERYTHING WORKS

### Verify application.properties is Properly Ignored

```bash
cd "/home/dms/Insync/davesnyd@gmail.com/Google Drive/Documents/development/photoSort/PhotoSort-V1"

# Check if file is ignored
git check-ignore PhotoSortServices/src/main/resources/application.properties

# Should output: PhotoSortServices/src/main/resources/application.properties
# If nothing is output, IT'S NOT IGNORED - STOP AND FIX!

# Make a test change
echo "# test comment" >> PhotoSortServices/src/main/resources/application.properties

# Verify git doesn't see it
git status

# Should NOT show application.properties as modified
# If it does show up, YOUR .GITIGNORE IS BROKEN - STOP AND FIX!

# Remove test comment
git checkout PhotoSortServices/src/main/resources/application.properties
```

### Test Database Connection

```bash
cd PhotoSortServices
mvn spring-boot:run

# Watch for errors in startup:
# - Should connect to PostgreSQL successfully
# - Should show "HikariPool" connection pool starting
# - Should show "Started PhotoSortApplication"
```

### Test OAuth Login

```bash
# Backend running from previous step
# In new terminal:
cd photosort-frontend
npm start

# Open browser: http://localhost:3000
# Click "Login with Google"
# Should successfully authenticate
```

### Test Git Polling (Optional)

```bash
# If you're using the Git polling feature:
# Watch the logs for:
# - "Polling git repository"
# - Should not show authentication errors
```

---

## 5️⃣ FINAL SECURITY CHECKLIST

- [ ] Postgres username and password filled in application.properties
- [ ] Old Google OAuth client DELETED from Google Cloud Console
- [ ] New Google OAuth client CREATED with new credentials
- [ ] New OAuth client ID and secret in application.properties
- [ ] Old GitHub token REVOKED (if applicable)
- [ ] New GitHub token CREATED (if applicable)
- [ ] New GitHub token in application.properties (if applicable)
- [ ] Verified application.properties is in .gitignore
- [ ] Tested git status doesn't show application.properties
- [ ] Backend starts without errors
- [ ] OAuth login works
- [ ] Database connection works

---

## 📝 EXAMPLE application.properties

Here's what your final `application.properties` should look like (with YOUR actual values):

```properties
# Copyright 2025, David Snyderman

# Application name
spring.application.name=PhotoSortServices

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/PhotoSortData
spring.datasource.username=postgres
spring.datasource.password=your_actual_postgres_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Connection Pool Configuration
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.com.photoSort=DEBUG

# Server Configuration
server.port=8080

# OAuth 2.0 Configuration (Google)
spring.security.oauth2.client.registration.google.client-id=123456789-abc123.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=GOCSPX-abc123def456ghi789
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/{registrationId}

# Session Configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false

# Git Configuration
git.repo.path=/home/dms/Insync/davesnyd@gmail.com/Google Drive/Documents/development/photoSort/Photos
git.repo.url=https://github.com/davesnyd/PhotoSort-Photos.git
git.username=davesnyd
git.token=ghp_abc123def456ghi789jkl012
git.poll.interval.minutes=5

# STAG Configuration
stag.script.path=./stag-main/stag.py
stag.python.executable=python3
```

---

## 🆘 TROUBLESHOOTING

### "Access Denied" for Database

```bash
# Check postgres is running
sudo systemctl status postgresql

# Check you can connect manually
psql -U postgres -d PhotoSortData

# Reset postgres password if needed
sudo -u postgres psql
ALTER USER postgres WITH PASSWORD 'new_password';
```

### OAuth "Redirect URI Mismatch"

- Ensure redirect URI in Google Console EXACTLY matches:
  `http://localhost:8080/login/oauth2/code/google`
- No trailing slash
- Check for typos
- May need to wait a few minutes for Google to propagate changes

### "application.properties not found"

```bash
# Check file exists
ls -la PhotoSortServices/src/main/resources/application.properties

# If missing, copy from template
cp PhotoSortServices/src/main/resources/application.properties.template \
   PhotoSortServices/src/main/resources/application.properties

# Then edit with your credentials
nano PhotoSortServices/src/main/resources/application.properties
```

### Git Still Tracking application.properties

```bash
# Remove from git tracking (but keep file locally)
git rm --cached PhotoSortServices/src/main/resources/application.properties

# Verify .gitignore has the entry
grep "application.properties" .gitignore

# Commit the removal
git commit -m "Stop tracking application.properties"
git push origin master
```

---

## 📞 NEED HELP?

If you run into issues:
1. Check the error message carefully
2. Review this guide step-by-step
3. Verify all credentials are correct
4. Check firewall/network settings
5. Review Spring Boot startup logs for specific errors
