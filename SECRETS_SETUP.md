# Secrets and Configuration Setup

## ⚠️ IMPORTANT SECURITY NOTICE

**NEVER commit files with actual credentials to git!**

Files containing secrets are listed in `.gitignore`:
- `application.properties` (backend)
- `.env` (frontend)
- Any files ending in `.local` or containing credentials

---

## Backend Configuration (Spring Boot)

### 1. Create `application.properties`

```bash
cd PhotoSortServices/src/main/resources/
cp application.properties.template application.properties
```

### 2. Fill in OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Create OAuth 2.0 Client ID (or use existing)
3. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID and Client Secret
5. Update `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_ACTUAL_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_ACTUAL_CLIENT_SECRET
```

### 3. Fill in Database Credentials

```properties
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
```

### 4. Fill in Git Credentials (if using Git polling)

```properties
git.repo.path=/home/dms/Insync/davesnyd@gmail.com/Google Drive/Documents/development/photoSort/Photos
git.repo.url=https://github.com/davesnyd/PhotoSort-Photos.git
git.username=davesnyd
git.token=ghp_yourPersonalAccessTokenHere
```

---

## Frontend Configuration (React)

### 1. Create `.env` file

```bash
cd photosort-frontend/
touch .env
```

### 2. Add configuration

```bash
REACT_APP_API_BASE_URL=http://localhost:8080
```

---

## Production Deployment

For production, use environment variables instead of hardcoded values:

### Spring Boot (Backend)

```bash
export OAUTH_CLIENT_ID="your-client-id"
export OAUTH_CLIENT_SECRET="your-client-secret"
export DB_USERNAME="postgres"
export DB_PASSWORD="your-db-password"
export GIT_TOKEN="your-git-token"
```

Then update `application.properties` to reference them:

```properties
spring.security.oauth2.client.registration.google.client-id=${OAUTH_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${OAUTH_CLIENT_SECRET}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
git.token=${GIT_TOKEN}
```

### React (Frontend)

```bash
export REACT_APP_API_BASE_URL="https://your-production-domain.com"
```

---

## Credential Rotation After Exposure

If credentials are ever exposed in git:

1. **Immediately rotate ALL exposed credentials**:
   - Delete old OAuth client in Google Cloud Console
   - Create new OAuth client with new credentials
   - Revoke and recreate GitHub personal access tokens
   - Change database passwords

2. **Clean git history**:
   ```bash
   # Using git-filter-repo (recommended)
   git filter-repo --path PhotoSortServices/src/main/resources/application.properties --invert-paths
   git filter-repo --path photosort-frontend/.env --invert-paths

   # Force push to remote
   git push origin --force --all
   ```

3. **Request GitHub cache purge**:
   - Contact GitHub support to purge from their cache
   - Or make repository private temporarily

4. **Verify .gitignore** is properly configured before committing new changes

---

## Verifying Security

Run these commands to check for exposed secrets:

```bash
# Check current files aren't tracked
git ls-files | grep -E "(application\.properties$|\.env$)"

# Should return nothing - if files appear, they're tracked (BAD!)

# Check history for secrets
git log --all --full-history -- "**/application.properties" "**/\.env"

# Check what's in .gitignore
cat .gitignore | grep -E "application\.properties|\.env"
```

---

## Quick Start for New Developers

1. Clone the repository
2. Copy template files:
   ```bash
   cp PhotoSortServices/src/main/resources/application.properties.template \
      PhotoSortServices/src/main/resources/application.properties
   ```
3. Get credentials from team lead / secure storage
4. Fill in `application.properties` and `.env`
5. **VERIFY** these files are in .gitignore before committing anything:
   ```bash
   git check-ignore PhotoSortServices/src/main/resources/application.properties
   git check-ignore photosort-frontend/.env
   ```
   Both should output the filename (confirming they're ignored)
