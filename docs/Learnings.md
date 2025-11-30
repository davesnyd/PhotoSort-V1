# Learnings

This document captures insights and lessons learned during the PhotoSort development process.

## Step 1: Database Configuration

- **Problem**: Initial project setup requires careful coordination between entity classes, schema.sql, and application.properties
  **Approach to Improve**: Create schema.sql first to have a clear database design reference, then map entities to match exactly. Use `spring.jpa.hibernate.ddl-auto=validate` in production to ensure schema and entities stay in sync.

- **Problem**: Test database configuration needs to be separate from production configuration
  **Approach to Improve**: Create application-test.properties with `spring.jpa.hibernate.ddl-auto=create-drop` for clean test runs. Use a separate test database to avoid conflicts.

- **Problem**: Cascade delete operations need careful planning to avoid unintended data loss
  **Approach to Improve**: Use `ON DELETE CASCADE` in foreign key definitions for child entities that should be deleted with their parent (like EXIF data with photos). Document cascade relationships clearly.

- **Problem**: Unique constraints need to be defined both at database level and JPA entity level
  **Approach to Improve**: Define unique constraints in both schema.sql and entity classes using `@UniqueConstraint` to ensure consistency and proper error handling.

## Step 3: Database Connection Configuration

- **Problem**: Default Spring Boot auto-configuration may not provide optimal performance for specific use cases
  **Approach to Improve**: Create explicit DatabaseConfig class with HikariCP configuration tuned for PostgreSQL. Use prepared statement caching and batch processing for better performance.

- **Problem**: Transaction management needs to be explicitly configured for complex operations
  **Approach to Improve**: Use `@EnableTransactionManagement` and configure `PlatformTransactionManager` explicitly. This ensures transactions are properly managed and can be rolled back on errors.

- **Problem**: Connection pool sizing can impact application performance
  **Approach to Improve**: Configure HikariCP with appropriate pool size (max 10, min idle 5). Monitor connection usage and adjust based on actual load. Use connection test query to validate connections.

- **Problem**: Hibernate performance can be improved with batch processing
  **Approach to Improve**: Enable Hibernate batch processing with `hibernate.jdbc.batch_size=20` and order inserts/updates. This reduces database round trips for bulk operations.
