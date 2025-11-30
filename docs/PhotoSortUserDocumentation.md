# PhotoSort User Documentation

## Overview
PhotoSort is a web-based photo management system that automatically discovers and indexes photos from a Git repository, extracts metadata, and provides search and viewing capabilities.

## Step 1: Database Configuration

### Functionality Created
**Database Setup and Configuration**

The database is the foundation of the PhotoSort system. It stores all information about users, photos, metadata, tags, and system configuration.

### System Setup (Administrator Only)

#### Prerequisites
- PostgreSQL 13 or higher installed
- Java 17 or higher installed
- Maven 3.8 or higher installed

#### Database Setup Steps
1. **Create the database**:
   - Open PostgreSQL command line or GUI tool
   - Create database: `CREATE DATABASE PhotoSortData;`
   - For testing: `CREATE DATABASE PhotoSortDataTest;`

2. **Set environment variables**:
   - `DB_USERNAME`: Your PostgreSQL username
   - `DB_PASSWORD`: Your PostgreSQL password

   Example (Linux/Mac):
   ```bash
   export DB_USERNAME=postgres
   export DB_PASSWORD=yourpassword
   ```

3. **Run the schema script**:
   - Navigate to PhotoSortServices/src/main/resources/
   - Execute schema.sql against PhotoSortData database

4. **Start the application**:
   ```bash
   cd PhotoSortServices
   mvn spring-boot:run
   ```

### Expected Results
- Application starts successfully
- Database connection established
- All tables created (users, photos, exif_data, etc.)
- No errors in console output

### Troubleshooting
- **Connection refused**: Verify PostgreSQL is running and credentials are correct
- **Database not found**: Ensure PhotoSortData database was created
- **Authentication failed**: Check DB_USERNAME and DB_PASSWORD environment variables

---

*Additional user functionality will be documented as features are implemented.*
