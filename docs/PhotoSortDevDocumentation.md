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

---

## Step 4: OAuth 2.0 Google Authentication

### Functionality Created
**Google OAuth 2.0 Integration with Spring Security**

Implemented complete authentication system using Google OAuth 2.0, including user management, session handling, and security configuration.

### Implementation Components

#### 1. Security Configuration (`SecurityConfig.java`)
- **Path**: `PhotoSortServices/src/main/java/com/photoSort/config/SecurityConfig.java`
- **Purpose**: Configures Spring Security with OAuth 2.0 and session management
- **Key Features**:
  - OAuth 2.0 login with Google as provider
  - Custom OAuth user service integration
  - CSRF protection enabled
  - Session-based authentication
  - Public endpoints: `/login`, `/oauth2/**`, `/error`
  - Protected endpoints: `/api/**` requires authentication

#### 2. Custom OAuth User Service (`CustomOAuth2UserService.java`)
- **Path**: `PhotoSortServices/src/main/java/com/photoSort/service/CustomOAuth2UserService.java`
- **Purpose**: Handles OAuth user information and integrates with PhotoSort user model
- **Responsibilities**:
  - Loads user from Google OAuth response
  - Creates new PhotoSort user on first login
  - Updates last login timestamp
  - Maps Google attributes to User entity

#### 3. User Management Service (`UserManagementService.java`)
- **Path**: `PhotoSortServices/src/main/java/com/photoSort/service/UserManagementService.java`
- **Purpose**: Business logic for user operations
- **Methods**:
  - `createUser(googleId, email, displayName)`: Create new user with default "USER" type
  - `updateUserType(userId, userType)`: Promote/demote user permissions
  - `findByGoogleId(googleId)`: Retrieve user by Google ID
  - `findByEmail(email)`: Retrieve user by email

#### 4. Authentication REST Controller (`AuthController.java`)
- **Path**: `PhotoSortServices/src/main/java/com/photoSort/controller/AuthController.java`
- **Endpoints**:
  - `GET /api/auth/current`: Returns currently authenticated user
  - `POST /api/auth/logout`: Invalidates session and logs out user

### Architecture Patterns

#### OAuth 2.0 Flow
1. User clicks "Login with Google"
2. Redirected to `/oauth2/authorization/google`
3. Spring Security redirects to Google OAuth consent screen
4. User grants permission
5. Google redirects back with authorization code
6. Spring Security exchanges code for access token
7. `CustomOAuth2UserService` loads user information
8. User session created
9. User redirected to frontend

#### Session Management
- Uses HTTP sessions (cookie-based)
- Session cookie name: `JSESSIONID`
- Session persists across requests
- Invalidated on logout

### Configuration

#### application.properties
```properties
# OAuth 2.0 Configuration
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
```

#### Environment Variables Required
- `GOOGLE_CLIENT_ID`: OAuth client ID from Google Cloud Console
- `GOOGLE_CLIENT_SECRET`: OAuth client secret
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

### Database Integration

#### User Entity Updates
- `google_id`: Unique identifier from Google (VARCHAR 255, UNIQUE, NOT NULL)
- `email`: User's Google email (VARCHAR 255, UNIQUE, NOT NULL)
- `display_name`: User's name from Google profile
- `user_type`: "USER" or "ADMIN"
- `first_login_date`: Timestamp of account creation
- `last_login_date`: Updated on each login

### Security Features

1. **CSRF Protection**: Enabled for all state-changing operations
2. **Session Fixation Protection**: New session created after authentication
3. **Secure Cookies**: HTTPOnly and Secure flags set in production
4. **OAuth Scopes**: Only requests profile and email (minimal permissions)

### Limitations

- Only Google OAuth supported (no other providers yet)
- Session storage in memory (consider Redis for production scaling)
- No remember-me functionality
- No multi-factor authentication
- Single role per user (USER or ADMIN)

### Expectations

- Google OAuth client must be properly configured in Google Cloud Console
- Authorized redirect URIs must include: `http://localhost:8080/login/oauth2/code/google`
- Users must have active Google accounts
- Frontend must handle OAuth redirect flow
- CORS must be configured if frontend on different origin

### Development Notes

1. **Testing OAuth Locally**:
   - Use Google Cloud Console to create OAuth client
   - Add `http://localhost:8080` to authorized origins
   - Add redirect URI to authorized redirect URIs
   - Store credentials in environment variables

2. **Adding New OAuth Providers**:
   - Add configuration to `application.properties`
   - Update `SecurityConfig` with new provider
   - May need custom user service for provider-specific attributes

3. **Debugging Authentication Issues**:
   - Enable security logging: `logging.level.org.springframework.security=DEBUG`
   - Check OAuth consent screen configuration
   - Verify redirect URIs match exactly
   - Inspect network tab for OAuth redirects

---

## Step 5: React Frontend Application

### Functionality Created
**Modern React-Based Web Interface**

Created complete React frontend application with routing, authentication state management, and responsive UI components.

### Project Structure

```
photosort-frontend/
├── public/                          # Static assets
├── src/
│   ├── components/                  # Reusable React components
│   │   ├── Navigation.js            # Top navigation bar
│   │   └── ProtectedRoute.js        # Route protection wrapper
│   ├── pages/                       # Page components
│   │   ├── Login.js                 # Login page with Google OAuth
│   │   ├── Home.js                  # Main dashboard
│   │   └── OAuthCallback.js         # OAuth redirect handler
│   ├── services/                    # API and business logic
│   │   ├── api.js                   # Axios instance configuration
│   │   └── authService.js           # Authentication API calls
│   ├── context/                     # React Context providers
│   │   └── AuthContext.js           # Authentication state management
│   ├── utils/                       # Utility functions (future)
│   ├── styles/                      # CSS stylesheets
│   │   ├── Login.css                # Login page styles
│   │   ├── Home.css                 # Home page styles
│   │   └── Navigation.css           # Navigation styles
│   ├── App.js                       # Root component with routing
│   └── index.js                     # Application entry point
├── .env                             # Environment configuration
├── package.json                     # Dependencies and scripts
└── README.md                        # React documentation
```

### Key Components

#### 1. Authentication Context (`AuthContext.js`)
- **Purpose**: Global authentication state management
- **State**:
  - `user`: Current user object
  - `isAuthenticated`: Boolean authentication status
  - `loading`: Loading state during auth check
- **Methods**:
  - `login(userData, token)`: Store auth data and update state
  - `logout()`: Clear auth data and log out
  - `loginWithGoogle()`: Initiate OAuth flow
  - `checkAuthStatus()`: Verify current authentication
- **Storage**: Uses `localStorage` for persistence
- **Hooks**: `useAuth()` custom hook for accessing context

#### 2. API Service (`api.js`)
- **Purpose**: Centralized Axios HTTP client
- **Configuration**:
  - Base URL: From `REACT_APP_API_BASE_URL` env variable
  - Default headers: `Content-Type: application/json`
  - Credentials: `withCredentials: true` for cookies
- **Interceptors**:
  - Request: Adds `Authorization` header with Bearer token
  - Response: Handles 401 errors (auto-redirect to login)

#### 3. Authentication Service (`authService.js`)
- **Methods**:
  - `loginWithGoogle()`: Redirects to backend OAuth endpoint
  - `getCurrentUser()`: GET `/api/auth/current`
  - `logout()`: POST `/api/auth/logout`
  - `isAuthenticated()`: Checks localStorage for token
  - `getStoredUser()`: Retrieves user from localStorage
  - `storeAuth(user, token)`: Saves auth data to localStorage

#### 4. Protected Route Component (`ProtectedRoute.js`)
- **Purpose**: Wrapper for routes requiring authentication
- **Features**:
  - Redirects to `/login` if not authenticated
  - Shows loading state while checking auth
  - Supports `adminOnly` prop for admin-only routes
  - Displays access denied for non-admin users

#### 5. Navigation Component (`Navigation.js`)
- **Purpose**: Top navigation bar
- **Visibility**: Only shown to authenticated users
- **Links**:
  - All Users: Home, My Photos
  - Admins: Users, Photos, Scripts, Configuration
- **User Info**: Displays user name and logout button
- **Styling**: Navy blue background, cream text (brand colors)

#### 6. Login Page (`Login.js`)
- **Elements**:
  - PhotoSort branding
  - Google OAuth button with icon
  - Instructions for users
- **Behavior**:
  - Redirects to `/` if already authenticated
  - Shows loading state during auth check
  - Initiates OAuth flow on button click
- **Styling**: Gradient background (navy to burgundy)

#### 7. OAuth Callback Handler (`OAuthCallback.js`)
- **Purpose**: Handles redirect from Google OAuth
- **Flow**:
  1. Called when user returns from Google
  2. Fetches current user from backend
  3. Stores auth data in context and localStorage
  4. Redirects to home page
- **Error Handling**: Shows error message, redirects to login

#### 8. Home Page (`Home.js`)
- **Content**:
  - Welcome message with user's name
  - Feature overview cards
  - PhotoSort capabilities highlights
- **Styling**: Cream background, responsive grid layout

### React Router Configuration

```javascript
Routes:
  Public:
    /login                  → Login Page
    /auth/callback         → OAuth Callback Handler

  Protected:
    /                      → Home Page
    /my-photos            → My Photos (placeholder)

  Protected (Admin Only):
    /users                → User Management (placeholder)
    /photos               → Photo Management (placeholder)
    /scripts              → Scripts Management (placeholder)
    /configuration        → Configuration (placeholder)
```

### State Management

#### React Context API
- **AuthContext**: Authentication state
- Provider wraps entire app
- Available via `useAuth()` hook in any component

#### React Query (TanStack Query)
- Configured with QueryClientProvider
- Ready for data fetching in future steps
- Default options:
  - Retry: 1 attempt
  - Refetch on window focus: Disabled

### Styling Approach

#### Color Scheme (Brand Consistency)
- Primary (Burgundy): `#800020`
- Secondary (Navy Blue): `#000080`
- Accent (Cream): `#FFFDD0`

#### CSS Organization
- Component-specific CSS files
- Class naming: BEM-inspired (e.g., `.login-container`, `.nav-link`)
- Responsive design with media queries
- Flexbox and Grid layouts

### Environment Configuration

#### .env File
```
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_OAUTH_REDIRECT_URI=http://localhost:3000/auth/callback
```

### Build and Deployment

#### Development
```bash
npm start                 # Start dev server (port 3000)
npm test                  # Run tests
```

#### Production
```bash
npm run build            # Create optimized build
npm run build && serve -s build    # Build and serve
```

#### Build Output
- Location: `build/` directory
- Optimized: Minified, tree-shaken, code-split
- File sizes: ~100KB JS (gzipped), ~1.4KB CSS

### Dependencies

#### Core
- `react`: ^18.x - UI library
- `react-dom`: ^18.x - React DOM rendering
- `react-router-dom`: ^7.x - Client-side routing
- `axios`: Latest - HTTP client
- `@tanstack/react-query`: Latest - Server state management

#### Dev Dependencies
- `react-scripts`: CRA build tools
- ESLint, Babel, Webpack (via CRA)

### Limitations

- React Router v7 requires Node 20+, using Node 18 (warnings expected)
- No offline support (PWA not configured)
- No server-side rendering (client-side only)
- localStorage not encrypted (tokens in plain text)
- No token refresh mechanism
- Session timeout handling basic (401 redirect only)

### Expectations

- Backend must run on `http://localhost:8080`
- Backend must support CORS for `http://localhost:3000`
- Backend must return user object from `/api/auth/current`
- OAuth flow must complete with session cookie
- Modern browser with localStorage support

### Development Notes

1. **Adding New Pages**:
   - Create component in `src/pages/`
   - Add route in `App.js`
   - Wrap with `<ProtectedRoute>` if auth required
   - Use `adminOnly` prop if admin-only

2. **Making API Calls**:
   ```javascript
   import api from '../services/api';
   const response = await api.get('/api/endpoint');
   ```

3. **Using Authentication**:
   ```javascript
   import { useAuth } from '../context/AuthContext';
   const { user, isAuthenticated, logout } = useAuth();
   ```

4. **Debugging**:
   - Check React DevTools for component state
   - Check Network tab for API calls
   - Check Application tab for localStorage
   - Check Console for errors

5. **ESLint Warnings**:
   - `useCallback` dependencies handled in context
   - Production build has no warnings
   - Development may show exhaustive-deps warnings