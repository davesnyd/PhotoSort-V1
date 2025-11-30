# PhotoSort Developer Documentation

## Architecture Overview
PhotoSort is built using:
- **Backend**: Spring Boot 3.2 with Java 17
- **Database**: PostgreSQL 13+
- **ORM**: Hibernate/JPA
- **Testing**: JUnit 5 with Spring Boot Test

## Step 1: Database Configuration

### Functionality Created
**Database Schema and JPA Entity Layer**

This step establishes the complete database schema and object-relational mapping layer for the PhotoSort application.

### Implementation Details

#### Database Schema
The schema consists of 11 tables organized into functional groups:

**User Management**:
- `users`: Stores user accounts with OAuth information

**Photo Storage**:
- `photos`: Core photo information and file metadata
- `exif_data`: EXIF metadata extracted from photos
- `photo_metadata`: Custom metadata fields and values
- `metadata_fields`: Definitions of custom metadata fields

**Tagging System**:
- `tags`: Tag definitions
- `photo_tags`: Many-to-many junction table for photo-tag associations

**Access Control**:
- `photo_permissions`: Grants specific users access to private photos

**User Preferences**:
- `user_column_preferences`: Customizable column display preferences

**Script Management**:
- `scripts`: Automated scripts for photo processing
- `script_execution_log`: Execution history and error logging

#### JPA Entity Classes
Each table has a corresponding entity class in `com.photoSort.model`:

- `User.java`: User entity with OAuth fields
- `Photo.java`: Photo entity with file metadata
- `ExifData.java`: EXIF metadata with BigDecimal for GPS coordinates
- `MetadataField.java`: Custom field definitions
- `PhotoMetadata.java`: Custom field values
- `Tag.java`: Tag definitions
- `PhotoTag.java`: Photo-tag associations
- `PhotoPermission.java`: Access permissions
- `UserColumnPreference.java`: User preferences
- `Script.java`: Script definitions
- `ScriptExecutionLog.java`: Execution logs

All entities use:
- Lombok annotations (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) for boilerplate reduction
- `@Entity` and `@Table` annotations for JPA mapping
- `@Index` annotations for database indexes
- `@UniqueConstraint` for unique constraints
- `FetchType.LAZY` for relationships to optimize performance

#### JPA Repositories
Repository interfaces extend `JpaRepository` and provide:
- Standard CRUD operations
- Custom query methods using Spring Data naming conventions
- Type-safe queries without writing SQL

Example custom query methods:
- `UserRepository.findByGoogleId(String googleId)`
- `PhotoRepository.findByOwner(User owner)`
- `TagRepository.findByTagValue(String tagValue)`

### Design Patterns Used

1. **Repository Pattern**: Separates data access logic from business logic
2. **Entity-Repository Pattern**: Each entity has a corresponding repository
3. **Lazy Loading**: Relationships use LAZY fetch to prevent N+1 query problems
4. **Cascade Operations**: Foreign keys with cascade delete for dependent entities

### Limitations

- `spring.jpa.hibernate.ddl-auto=validate` in production requires manual schema migration
- Cascade deletes are aggressive - deleting a photo deletes all related data
- No soft delete support in this version
- No audit fields (created_by, updated_by) - only timestamps

### Expectations

- PostgreSQL must be running and accessible
- Environment variables DB_USERNAME and DB_PASSWORD must be set
- Schema must be manually created before first run (use schema.sql)
- For tests, a separate database (PhotoSortDataTest) should exist

### Testing

Comprehensive test suite in `DatabaseSchemaTest.java` covers:
- Database connectivity
- CRUD operations on all entities
- Foreign key relationships
- Cascade delete behavior
- Unique constraint enforcement
- Repository query methods

Run tests with:
```bash
mvn test
```

### Configuration Files

**application.properties**:
- Main application configuration
- Database connection settings
- JPA/Hibernate configuration
- Connection pooling (HikariCP)

**application-test.properties**:
- Test-specific configuration
- Uses `create-drop` to reset database between tests
- Connects to separate test database

**schema.sql**:
- DDL for creating all tables
- Includes DROP statements for clean setup
- Defines all indexes, foreign keys, and constraints

### Dependencies

Key Maven dependencies:
- `spring-boot-starter-data-jpa`: JPA and Hibernate
- `postgresql`: PostgreSQL JDBC driver
- `lombok`: Annotation processing for boilerplate code
- `spring-boot-starter-test`: Testing framework

### Development Notes

1. **Adding a new entity**:
   - Create entity class in `model` package
   - Create repository interface in `repository` package
   - Add table creation SQL to `schema.sql`
   - Write tests in test package

2. **Modifying the schema**:
   - Update entity class annotations
   - Update schema.sql
   - Create migration script for production
   - Update tests to cover new fields

3. **Performance optimization**:
   - Indexes are defined for all foreign keys and frequently queried fields
   - Use LAZY loading for relationships
   - Connection pooling configured (max 10 connections)

### Future Enhancements

- Database migration tool (Flyway or Liquibase)
- Audit fields and audit logging
- Soft delete capability
- Database sharding for large photo collections
- Read replicas for query scaling

---

## Step 3: Database Connection Configuration

### Functionality Created
**Advanced Database Connection Management**

This step adds explicit configuration for database connectivity, connection pooling, and transaction management to optimize performance and reliability.

### Implementation Details

#### DatabaseConfig Class
Created `com.photoSort.config.DatabaseConfig` with:

**DataSource Configuration**:
- HikariCP connection pool (industry-leading performance)
- Configurable pool size (default: max 10, min idle 5)
- Connection timeout: 30 seconds
- Connection test query for health checks
- Prepared statement caching enabled

**Entity Manager Configuration**:
- Scans `com.photoSort.model` package for entities
- Hibernate as JPA provider
- PostgreSQL dialect
- Batch processing enabled (batch size: 20)
- Ordered inserts/updates for efficiency

**Transaction Manager**:
- JPA-based transaction management
- Automatic rollback on exceptions
- Support for nested transactions
- Integration with Spring's `@Transactional`

### Configuration Properties

**Connection Pool Settings**:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

**Hibernate Performance Settings**:
```properties
hibernate.jdbc.batch_size=20
hibernate.order_inserts=true
hibernate.order_updates=true
hibernate.jdbc.batch_versioned_data=true
```

### Design Patterns Used

1. **Factory Pattern**: EntityManagerFactory creates EntityManager instances
2. **Singleton Pattern**: DataSource is application-scoped singleton
3. **Template Pattern**: TransactionTemplate for programmatic transactions
4. **Proxy Pattern**: Spring uses proxies for `@Transactional` methods

### Testing

Additional test class `DatabaseConnectionConfigTest` covers:
- Database connection establishment
- Entity mapping validation for all 11 entities
- CRUD operations with foreign key constraints
- Cascade delete behavior
- Hibernate SQL generation
- Transaction rollback on errors

### Performance Optimizations

1. **Connection Pooling**: Reuses database connections instead of creating new ones
2. **Prepared Statement Caching**: Caches compiled SQL statements (250 statements, 2048 chars each)
3. **Batch Processing**: Groups multiple SQL operations to reduce round trips
4. **Ordered Operations**: Minimizes database deadlocks by ordering inserts/updates

### Limitations

- Second-level cache disabled (can be enabled later with Redis/Hazelcast)
- Query cache disabled (enable when read-heavy workload identified)
- Connection pool size fixed (should be tuned based on actual load)
- No read-write splitting (can add read replicas later)

### Expectations

- PostgreSQL must support prepared statement caching
- Database must handle batch operations efficiently
- Connection pool size appropriate for expected concurrent users
- Transaction isolation level is READ_COMMITTED (PostgreSQL default)

### Monitoring

Key metrics to monitor:
- Active connections (should stay below maximum pool size)
- Connection wait time (should be minimal)
- Transaction duration (long transactions may indicate issues)
- Query execution time (identify slow queries)

HikariCP provides JMX beans for monitoring:
- `HikariPoolMXBean` for pool statistics
- Active/idle connection counts
- Connection acquisition time

### Development Notes

1. **Adding transactional methods**:
   - Annotate service methods with `@Transactional`
   - Keep transactions as short as possible
   - Avoid external calls within transactions

2. **Tuning connection pool**:
   - Monitor connection usage under load
   - Formula: connections = ((core_count * 2) + effective_spindle_count)
   - For 4 cores with SSD: 8-10 connections is optimal

3. **Debugging transaction issues**:
   - Enable transaction logging: `logging.level.org.springframework.transaction=DEBUG`
   - Check for `@Transactional` on interfaces vs implementations
   - Verify proxy creation for transactional beans
