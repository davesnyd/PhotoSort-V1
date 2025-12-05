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
  **Approach to Improve**: Use both in-memory configuration override map AND file persistence. The in-memory map allows configuration changes to take effect immediately without requiring a restart, while persisting to application.properties ensures changes survive restarts. Use Java Properties class to safely read, update, and write the properties file while preserving all existing properties.

- **Problem**: Password fields need to be redacted when displaying configuration but only updated when actually changed
  **Approach to Improve**: Always redact passwords to "********" in GET responses. In PUT requests, only update password fields if the value is not "********". This prevents accidentally overwriting passwords with the redacted placeholder.

- **Problem**: Frontend tests failing due to multiple elements with same value (password fields)
  **Approach to Improve**: Use `getAllByDisplayValue()` instead of `getByDisplayValue()` when testing elements that may have duplicate values. Check the count of matched elements is greater than zero rather than testing for a specific element.

- **Problem**: Configuration DTO needs proper nested structure for JSON deserialization
  **Approach to Improve**: Create properly structured DTO classes with nested static classes for each configuration section (Database, Git, OAuth, STAG). This ensures Jackson can serialize/deserialize the configuration correctly.

- **Problem**: Test configuration object creation is repetitive across multiple test cases
  **Approach to Improve**: Create a helper method `createTestConfiguration()` that returns a properly initialized ConfigurationDTO with all nested objects. This reduces code duplication and makes tests more maintainable.

- **Problem**: Tests that call ConfigService.updateConfiguration() persist test data to application.properties file, corrupting production configuration
  **Approach to Improve**: Tests should either: (1) Mock the ConfigService.saveToPropertiesFile() method to prevent file writes during testing, OR (2) Use a test-specific properties file location that doesn't affect src/main/resources/application.properties. The current implementation writes directly to the production properties file during tests, which is a critical issue that corrupts configuration values.

## Step 14: Git Repository Polling Service

- **Problem**: @Scheduled annotation timing units can be confusing (milliseconds vs minutes vs seconds)
  **Approach to Improve**: Use SpEL expressions with clear unit conversion in @Scheduled annotations. For example: `#{${git.poll.interval.minutes:5} * 60 * 1000}` makes it clear that configuration is in minutes but scheduling is in milliseconds. Always document the expected units in configuration properties.

- **Problem**: Git operations require careful error handling to prevent service from crashing on repository issues
  **Approach to Improve**: Wrap all Git operations in try-catch blocks and log errors gracefully. Return null or empty results on failures rather than throwing exceptions. This allows the scheduled task to continue running even when Git operations fail temporarily.

- **Problem**: ConfigService methods may need to be accessed by other services but are initially private
  **Approach to Improve**: Design service methods with appropriate visibility from the start. If a method might be useful to other services, make it public. Private methods should only be used for internal implementation details that won't be needed elsewhere.

- **Problem**: JGit diff operations can be complex with multiple steps (tree iterators, parsers, diff formatting)
  **Approach to Improve**: Create helper methods for common JGit operations (e.g., `getCanonicalTreeParser()`, `detectChangedImageFiles()`). This makes the code more readable and reusable. Document each step of the Git operations for future maintenance.

## Step 15: EXIF Data Extraction

- **Problem**: Image dimensions (width/height) should be stored with Photo entity, not ExifData entity
  **Approach to Improve**: Keep clear separation of concerns - ExifData entity is specifically for camera/exposure metadata (make, model, GPS, exposure settings), while basic image properties (dimensions, file size, dates) belong on the Photo entity. Extract image dimensions separately using JpegDirectory when processing images.

- **Problem**: First poll of Git repository needs special handling to process all existing files
  **Approach to Improve**: In GitPollingService.detectChangedImageFiles(), when oldCommitHash is null (first poll), use TreeWalk to iterate through all files in the current commit tree. This ensures existing images are processed on initial setup.

- **Problem**: Not all image files have EXIF data (e.g., PNG screenshots, edited images)
  **Approach to Improve**: Handle null ExifData gracefully in processImageFile(). Create Photo record regardless of whether EXIF data exists. Only save ExifData if extraction returns non-null result. This allows the system to track all images while extracting metadata when available.

- **Problem**: EXIF Data extraction uses deprecated BigDecimal rounding mode constant
  **Approach to Improve**: BigDecimal.ROUND_HALF_UP is deprecated in favor of RoundingMode.HALF_UP. Update ExifDataService to use `setScale(8, RoundingMode.HALF_UP)` instead of the deprecated constant for GPS coordinate precision.

## Frontend Bug Fixes (Post Step 20)

- **Problem**: Functions passed to custom hooks with useEffect dependencies cause infinite render loops
  **Approach to Improve**: Always wrap functions passed to custom hooks in useCallback with proper dependency arrays. When a function is defined inside a component without useCallback, React creates a new function instance on every render. If that function is passed to a hook that includes it in a useEffect dependency array, it triggers infinite re-renders. Examples: Scripts.js fetchScriptsWithPagination and Photos.js fetchFunction both needed useCallback wrappers.

- **Problem**: Placeholder routes remain in production code after features are implemented
  **Approach to Improve**: When implementing features like Photos page, systematically search for and replace ALL placeholder routes in App.js. Use grep to find "This feature will be implemented" or similar placeholder text to ensure no routes are missed.
