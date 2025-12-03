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

## Step 4: OAuth 2.0 Google Authentication

- **Problem**: OAuth user information needs to be integrated with application's user model
  **Approach to Improve**: Create CustomOAuth2UserService that extends DefaultOAuth2UserService. In loadUser method, extract OAuth attributes and call UserService.processOAuthLogin to create/update user record.

- **Problem**: First-time users need different handling than returning users
  **Approach to Improve**: In UserService.processOAuthLogin, check if user exists by Google ID. If not found, create new user with USER type and set first login date. If found, only update last login date.

- **Problem**: OAuth configuration secrets should not be hardcoded
  **Approach to Improve**: Use environment variables for OAuth client ID and client secret. Reference them in application.properties with ${VARIABLE_NAME} syntax.

- **Problem**: Need to distinguish between regular users and administrators
  **Approach to Improve**: Add UserType enum (USER, ADMIN) to User entity. Default new users to USER type. Provide UserService.updateUserType method for administrators to promote users.

## Step 13: Configuration Management Page

- **Problem**: Updating application.properties at runtime doesn't take effect without application restart
  **Approach to Improve**: Use an in-memory configuration override map that takes precedence over application.properties. This allows configuration changes to take effect immediately without requiring a restart. For production, consider using Spring Cloud Config for externalized configuration.

- **Problem**: Password fields need to be redacted when displaying configuration but only updated when actually changed
  **Approach to Improve**: Always redact passwords to "********" in GET responses. In PUT requests, only update password fields if the value is not "********". This prevents accidentally overwriting passwords with the redacted placeholder.

- **Problem**: Frontend tests failing due to multiple elements with same value (password fields)
  **Approach to Improve**: Use `getAllByDisplayValue()` instead of `getByDisplayValue()` when testing elements that may have duplicate values. Check the count of matched elements is greater than zero rather than testing for a specific element.

- **Problem**: Configuration DTO needs proper nested structure for JSON deserialization
  **Approach to Improve**: Create properly structured DTO classes with nested static classes for each configuration section (Database, Git, OAuth, STAG). This ensures Jackson can serialize/deserialize the configuration correctly.

- **Problem**: Test configuration object creation is repetitive across multiple test cases
  **Approach to Improve**: Create a helper method `createTestConfiguration()` that returns a properly initialized ConfigurationDTO with all nested objects. This reduces code duplication and makes tests more maintainable.
