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
