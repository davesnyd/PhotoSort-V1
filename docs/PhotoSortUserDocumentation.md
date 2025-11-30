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

## Step 3: Database Connection Configuration

### Functionality Created
**Optimized Database Connection Management**

The application now uses advanced connection pooling and transaction management for improved performance and reliability.

### What This Means for Users

#### Performance Improvements
- **Faster response times**: Connection pooling eliminates the overhead of creating new database connections
- **Better concurrency**: Up to 10 simultaneous users can access the database efficiently
- **Batch operations**: Multiple database operations are grouped together for speed

#### Reliability Improvements
- **Connection health checks**: Bad connections are automatically detected and replaced
- **Automatic rollback**: If an error occurs, database changes are automatically undone
- **Connection timeout**: Prevents hanging when database is slow to respond

### Technical Details (For Advanced Users)

#### Connection Pool Configuration
The application uses HikariCP with these settings:
- Maximum connections: 10
- Minimum idle connections: 5
- Connection timeout: 30 seconds

To monitor connection pool health:
1. Check application logs for "HikariPool" entries
2. Look for connection acquisition times
3. Watch for connection timeout warnings

#### Transaction Management
All data modifications are wrapped in transactions:
- Changes are saved together or not at all
- Prevents partial updates that could corrupt data
- Automatically retries on temporary failures

### Troubleshooting

**Slow database operations**:
- Check if connection pool is exhausted (all 10 connections in use)
- Look for long-running transactions in application logs
- Consider increasing pool size if consistently hitting limit

**Transaction rollback errors**:
- Check for unique constraint violations (duplicate data)
- Verify foreign key relationships are valid
- Review application logs for specific error messages

**Connection pool exhaustion**:
- Error: "Connection is not available, request timed out after 30000ms"
- Solution: Ensure database is accessible and responsive
- Solution: Check for connection leaks (connections not being closed)

---

*Additional user functionality will be documented as features are implemented.*
