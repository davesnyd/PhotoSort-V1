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

## Step 6: Admin Navigation and User Table Page

### Functionality Created
**Complete User Management System for Administrators**

This step implements a fully-featured user management interface with pagination, sorting, searching, and inline editing capabilities.

### Implementation Details

#### Backend Components

##### DTOs (Data Transfer Objects)
Created 5 new DTOs in `com.photoSort.dto` package:

1. **UserDTO.java**
   - Transfers user data to frontend with calculated photo count
   - Excludes sensitive `googleId` field for security
   - Factory method: `fromUser(User user, long photoCount)`
   - Fields: userId, email, displayName, userType, firstLoginDate, lastLoginDate, photoCount

2. **UserUpdateRequest.java**
   - Request body for updating user type
   - Field: `userType` (String: "USER" or "ADMIN")
   - Validation performed in controller layer

3. **PagedResponse<T>.java**
   - Generic pagination wrapper for any entity type
   - Fields: content, page, pageSize, totalPages, totalElements
   - Factory method: `fromPage(Page<T>)` converts Spring Data Page to custom format

4. **ApiResponse<T>.java**
   - Standardized API response format
   - Success format: `{success: true, data: {...}}`
   - Error format: `{success: false, error: {code: "ERROR_CODE", message: "..."}}`
   - Factory methods: `success(T data)`, `error(String code, String message)`

5. **SearchFilterDTO.java**
   - Advanced search filter criteria
   - Fields: column, value, operation (CONTAINS/NOT_CONTAINS)
   - Supports: displayName, email, userType, firstLoginDate, lastLoginDate

##### Repository Enhancements
**UserRepository.java** - Added 5 new methods:

```java
// Extended JpaSpecificationExecutor for dynamic queries
public interface UserRepository extends JpaRepository<User, Long>,
                                      JpaSpecificationExecutor<User> {

    // Optimized query with LEFT JOIN (avoids N+1 problem)
    @Query("SELECT u, COUNT(p) FROM User u LEFT JOIN Photo p ON p.owner = u GROUP BY u.userId")
    List<Object[]> findAllWithPhotoCounts(Pageable pageable);

    // Quick search with photo counts
    @Query("SELECT u, COUNT(p) FROM User u LEFT JOIN Photo p ON p.owner = u " +
           "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "GROUP BY u.userId")
    List<Object[]> searchUsersWithPhotoCounts(@Param("search") String search, Pageable pageable);

    // Pagination support
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE ...")
    long countSearchResults(@Param("search") String search);
}
```

**Performance Optimization:**
- Uses LEFT JOIN with GROUP BY instead of N+1 queries
- Single query fetches users with their photo counts
- Reduces database round trips from N+1 to 1

##### Service Layer
**UserService.java** - Added 3 new methods:

1. **getUsers(page, pageSize, sortBy, sortDir)**
   - Returns paginated list of users with photo counts
   - Uses `findAllWithPhotoCounts()` for optimal performance
   - Supports sorting on any user field

2. **searchUsers(searchTerm, page, pageSize, sortBy, sortDir)**
   - Quick search by email or display name
   - Case-insensitive LIKE query
   - Returns users with photo counts

3. **advancedSearchUsers(filters, page, pageSize, sortBy, sortDir)**
   - Dynamic filtering with JPA Specifications
   - Combines multiple filters with AND logic
   - Supports: CONTAINS, NOT_CONTAINS operations
   - Builds Specification from filter list

**Helper Method:**
```java
private Specification<User> buildSpecification(List<SearchFilterDTO> filters) {
    return (root, query, criteriaBuilder) -> {
        // Dynamically builds predicates from filters
        // Combines with AND logic
        // Handles string, enum, and date fields
    };
}
```

##### Controller
**UserController.java** - New REST controller:

**GET /api/users**
- Query params: page, pageSize, sortBy, sortDir, search (optional), filters (optional)
- Returns: `ApiResponse<PagedResponse<UserDTO>>`
- Authorization: Admin only (returns 403 if not admin)
- Supports three modes:
  1. All users (no search params)
  2. Quick search (search param provided)
  3. Advanced search (filters param provided)

**PUT /api/users/{userId}**
- Path param: userId
- Request body: `{userType: "USER" | "ADMIN"}`
- Returns: `ApiResponse<UserDTO>`
- Authorization: Admin only
- Validation: userType must be "USER" or "ADMIN"
- Updates user type and returns updated user

#### Frontend Components

##### Service Layer
**userService.js** (`src/services/userService.js`):
```javascript
getUsers(params) {
    // params: page, pageSize, sortBy, sortDir, search, filters
    // Returns: ApiResponse<PagedResponse<UserDTO>>
}

updateUserType(userId, userType) {
    // Updates user type via PUT /api/users/{userId}
    // Returns: ApiResponse<UserDTO>
}
```

##### Reusable Components

1. **PaginationControls** (`src/components/PaginationControls.js`)
   - Props: currentPage, totalPages, onPageChange
   - Features: First, Last, Previous, Next buttons
   - Smart page display (shows 5 pages at a time)
   - Hides when only 1 page exists

2. **QuickSearch** (`src/components/QuickSearch.js`)
   - Props: onSearch
   - Simple text input for email/name search
   - Clear button when search term exists
   - Submits on Enter key

3. **AdvancedSearch** (`src/components/AdvancedSearch.js`)
   - Props: onSearch
   - 3 filter rows (column, operation, value)
   - Column dropdown: Email, Display Name, User Type, First/Last Login Date
   - Operation dropdown: Contains, Not Contains
   - Apply Filters and Clear All buttons

4. **SearchControls** (`src/components/SearchControls.js`)
   - Props: onQuickSearch, onAdvancedSearch
   - Tabbed interface (Quick Search | Advanced Search)
   - Integrates QuickSearch and AdvancedSearch
   - Switches between search modes

5. **UserTable** (`src/components/UserTable.js`)
   - Props: users, onSortChange, onUserTypeChange, currentSort
   - Features:
     - Sortable columns (click header, shows ↑↓ icons)
     - Inline user type editing (dropdown with Save/Cancel)
     - "View Images" button (navigates to /photos/{userId})
     - Color-coded badges (blue=USER, red=ADMIN)
     - Formatted date display (e.g., "Nov 30, 2025, 2:30 PM")
   - Actions: Edit (toggles dropdown), View Images

##### Pages

1. **Users** (`src/pages/Users.js`)
   - Main user management page
   - State management:
     - Pagination: page, pageSize, totalPages, totalElements
     - Sorting: sortBy, sortDir
     - Search: searchMode (none/quick/advanced), quickSearchTerm, advancedFilters
   - Effects:
     - `useEffect` fetches users when dependencies change
     - Auto-resets page to 0 on search/filter change
   - Components used: SearchControls, UserTable, PaginationControls
   - Error and loading states displayed

2. **Photos** (`src/pages/Photos.js`)
   - Placeholder for user photo management
   - Receives userId from route param `/photos/:userId`
   - Displays feature list for Step 7
   - "Back to Users" button

##### Routing Updates
**App.js**:
```javascript
import Users from './pages/Users';
import Photos from './pages/Photos';

<Route path="/users" element={
    <ProtectedRoute adminOnly={true}>
        <Users />
    </ProtectedRoute>
} />

<Route path="/photos/:userId" element={
    <ProtectedRoute adminOnly={true}>
        <Photos />
    </ProtectedRoute>
} />
```

##### Styling
All components have dedicated CSS files with:
- Consistent color scheme (primary green: #4CAF50)
- Hover states and smooth transitions
- Responsive design (flex/grid layouts)
- Accessible button states (normal, hover, disabled)
- Table styling (alternating rows, borders, shadows)

### Design Patterns Used

1. **DTO Pattern**
   - Separates internal models from external API contracts
   - Allows adding computed fields (photoCount)
   - Hides sensitive data (googleId)

2. **Repository Pattern with Specifications**
   - `JpaSpecificationExecutor` for dynamic queries
   - Type-safe query building
   - Reusable filter logic

3. **Optimized Query Pattern**
   - LEFT JOIN with GROUP BY avoids N+1 problem
   - Single query instead of 1 + N queries
   - Significant performance improvement for large datasets

4. **Component Composition**
   - Small, focused components
   - Props for communication
   - Reusable across pages

5. **Container/Presentational Pattern**
   - Users.js (container): manages state, API calls
   - UserTable.js (presentational): receives props, renders UI

### Testing Status

#### Compilation
- Backend: ✅ BUILD SUCCESS (34 source files)
- Frontend: ✅ Compiled successfully

#### Unit Tests
⚠️ **Database Configuration Required:**

Tests fail with: `FATAL: password authentication failed for user "postgres"`

**Solution:** Set environment variables:
```bash
export DB_USERNAME=your_postgres_username
export DB_PASSWORD=your_postgres_password
export OAUTH_CLIENT_ID=test-client-id
export OAUTH_CLIENT_SECRET=test-client-secret
```

**Configuration Update:**
- `application-test.properties` now uses fallback defaults
- `${***REMOVED***}` and `${***REMOVED***}`
- Requires `PhotoSortDataTest` database to exist

### API Endpoints

```
GET  /api/users?page=0&pageSize=10&sortBy=email&sortDir=asc
GET  /api/users?search=john
GET  /api/users?filters=[{"column":"userType","value":"ADMIN","operation":"CONTAINS"}]
PUT  /api/users/{userId}
     Body: {"userType": "ADMIN"}
```

### Database Queries Generated

**Without optimization (N+1 problem):**
```sql
SELECT * FROM users LIMIT 10;          -- 1 query
SELECT COUNT(*) FROM photos WHERE owner_id = 1;  -- N queries
SELECT COUNT(*) FROM photos WHERE owner_id = 2;
...
```

**With optimization (single query):**
```sql
SELECT u.*, COUNT(p.photo_id) as photo_count
FROM users u
LEFT JOIN photos p ON p.owner_id = u.user_id
GROUP BY u.user_id
LIMIT 10;
```

### Security Considerations

1. **Authorization**
   - All endpoints check admin status
   - Returns 403 Forbidden if non-admin
   - Uses `CustomOAuth2User.isAdmin()` check

2. **Input Validation**
   - userType validated against enum
   - SQL injection prevented by JPA (no raw SQL)
   - Input sanitization in search (LIKE with parameters)

3. **Data Exposure**
   - UserDTO excludes sensitive googleId
   - Only necessary fields exposed to frontend

### Files Created/Modified

**Backend (9 files):**
- 5 new DTOs
- 1 modified repository (added 5 methods)
- 1 modified service (added 3 methods)
- 1 new controller
- 1 modified test properties

**Frontend (17 files):**
- 1 new service
- 5 new components + CSS
- 2 new pages + CSS
- 1 modified App.js

**Total: 27 files (21 new, 6 modified)**

### Expectations

**Backend:**
- PostgreSQL with PhotoSortData and PhotoSortDataTest databases
- Environment variables set for DB credentials
- Spring Boot running on port 8080

**Frontend:**
- React development server on port 3000
- Backend API accessible at http://localhost:8080
- Valid admin user authenticated

**Browser:**
- Modern browser with ES6 support
- JavaScript enabled
- localStorage available

### Known Limitations

1. **Advanced Search Photo Count**
   - Currently returns 0 for photo count in advanced search
   - Comment in code suggests future optimization with batch query
   - Quick search and all users view have accurate counts

2. **Filter Encoding**
   - Advanced filters sent as JSON string in query param
   - Alternative: POST endpoint for complex searches
   - Works but not ideal for large filter sets

3. **No Export Functionality**
   - Cannot export user list to CSV/Excel
   - Future enhancement

4. **No Bulk Operations**
   - Can only edit one user at a time
   - No bulk user type changes
   - Future enhancement

### Development Notes

1. **Adding New DTOs:**
   - Place in `com.photoSort.dto` package
   - Use Lombok for getters/setters
   - Add factory methods for conversions

2. **Adding Repository Methods:**
   - Use `@Query` for complex queries
   - Extend `JpaSpecificationExecutor` for dynamic queries
   - Always consider N+1 problem

3. **Building Specifications:**
   - Return `Specification<Entity>` lambda
   - Use `criteriaBuilder` for predicates
   - Combine with `and()`, `or()`, `not()`

4. **Frontend State Management:**
   - Use `useState` for component state
   - Use `useEffect` for side effects (API calls)
   - Reset page to 0 when search changes

5. **API Error Handling:**
   - Backend returns `ApiResponse` with error details
   - Frontend shows error messages to user
   - Check `response.success` before accessing `data`

### Next Steps (Step 7)

Replace Photos.js placeholder with:
- Photo upload functionality
- Photo list with thumbnails
- Photo deletion
- Photo metadata editing
- Public/private toggle
---

## Step 7: Photo Table Page

### Functionality Created
**Photo Management Interface with Permission-Based Viewing**

Implemented complete photo viewing system with permission-based filtering, search/pagination, admin capabilities, and reusable generic table components.

### Components Created

**Backend**:
- `PhotoDTO.java` - Data Transfer Object for photos
- `ColumnPreferenceDTO.java` - DTO for user column customization  
- `PhotoService.java` - Business logic with JPA Criteria API queries
- `PhotoController.java` - REST endpoints with permission handling
- Enhanced `UserController.java` with column preference endpoints
- `PhotoTablePageTest.java` - 10 comprehensive integration tests

**Frontend**:
- `photoService.js` - API client for photo operations
- `PhotoTable.js` - Photo table using generic DataTable component
- `PhotoTable.css` - Styling for photo display
- `Photos.js` - Main page using useTableData hook

**Generic/Reusable** (eliminating code duplication):
- `useTableData.js` - Custom hook for table state management
- `DataTable.js` - Generic sortable table component  
- `TablePage.js` - Page wrapper component

### Permission-Based Filtering Logic

Users can see three types of photos:
1. **Owned photos**: Current user is the owner
2. **Public photos**: isPublic = true
3. **Granted access**: PhotoPermission record exists

Implemented in `PhotoService.getPhotosForUser()` using JPA Criteria API predicates.

**Critical Implementation Note**: Predicates must be rebuilt for each query (main and count) to avoid Hibernate error "Already registered a copy".

Admins use separate method `getPhotosForAdmin()` with no permission restrictions.

### Authentication Handling

`PhotoController` uses `SecurityContextHolder` to support both OAuth2User (production) and @WithMockUser (testing):

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth.getPrincipal() instanceof OAuth2User) {
    userEmail = oauth2User.getAttribute("email");
} else {
    userEmail = auth.getName(); // @WithMockUser
}
```

### API Endpoints

**GET /api/photos** - List photos with permission filtering
- Query params: page, size, sort, direction, search, userId (admin), filter fields
- Returns: `ApiResponse<PagedResponse<PhotoDTO>>`

**GET /api/users/{userId}/columns** - Get column preferences
- Returns defaults if user has no preferences
- Returns: `ApiResponse<List<ColumnPreferenceDTO>>`

**PUT /api/users/{userId}/columns** - Save column preferences
- Body: `List<ColumnPreferenceDTO>`
- Returns: `ApiResponse<String>`

### Generic Component Architecture

Created reusable table infrastructure used by both Users and Photos pages:

**useTableData Hook**:
- Manages pagination, sorting, search state
- Handles API calls with loading/error states
- Parameters: `(fetchFunction, initialSort, initialPageSize)`
- Returns: data, loading, error, handlers, pagination state

**DataTable Component**:
- Configurable columns with optional custom renderers
- Built-in sorting UI with visual feedback
- Action buttons per row via render prop
- Props: data, columns, onSort, currentSort, renderActions, keyField

**Column Configuration Example**:
```javascript
{
  field: 'fileName',
  header: 'File Name',
  sortable: true,
  render: (row, value) => <span>{value}</span>  // Optional
}
```

### Testing Strategy

**PhotoTablePageTest.java** (10 integration tests):
1. User sees only authorized photos (owned + public + granted)
2. Admin sees all photos
3. Admin filters by specific user
4. Custom columns API returns user preferences
5. Default columns for new users
6. Column sorting (ascending/descending)
7. Pagination (multiple pages, correct counts)
8. Quick search (fileName, filePath)
9. Public photos visible to all users
10. Advanced search with multiple filters

Uses `@SpringBootTest`, `@AutoConfigureMockMvc`, `@WithMockUser`, `@Transactional`.

### Development Notes

**JPA Criteria API - Critical Gotcha**:
Never reuse Predicate objects between queries. Always rebuild predicates for main and count queries separately. Violating this causes "Already registered a copy" Hibernate error.

**Creating Generic Components**:
1. Identify common patterns across components
2. Extract to configurable generic component
3. Use props for customization (columns, data, callbacks)
4. Keep it simple - avoid over-abstraction

**Adding Photo Filters**:
1. Add query parameter to PhotoController.getPhotos()
2. Pass to PhotoService methods
3. Add predicate building logic
4. Update frontend photoService.js

**Authentication in Tests**:
- Use `@WithMockUser(username = "email@example.com")`
- Create matching user in @BeforeEach setup with that email
- SecurityContextHolder handles both OAuth2 and mock authentication

### Limitations

1. **No Photo Upload** - Photos added directly to database only
2. **No Thumbnail Generation** - Assumes pre-existing thumbnails
3. **No Column Customization UI** - Backend ready, frontend not implemented
4. **No Permission Management** - Can't grant/revoke access via UI
5. **Limited Advanced Filters** - Maximum 2 filters (easily expandable)
6. **No Photo Editing** - View-only (no delete, metadata edit, visibility toggle)

### Next Steps (Step 8+)

Future enhancements:
- Photo upload with automatic thumbnail generation
- Photo deletion and batch operations
- Metadata editing interface
- Public/private visibility toggle
- Permission management UI
- Column customization interface
- EXIF data extraction and display
- Photo tagging and categorization

## Step 8: Modify Columns Dialog

### Functionality Created
**Column Customization System**

Allows users to customize which columns appear in their photo table by selecting from standard columns, EXIF fields, and custom metadata fields.

### Implementation Details

#### Backend Components

**MetadataController.java**
- REST controller for metadata operations
- Endpoint: `GET /api/metadata/fields`
- Returns unified list of all available column options:
  - Standard columns (file_name, thumbnail, file_created_date, etc.)
  - EXIF fields (camera_make, camera_model, date_time_original, etc.)
  - Custom metadata fields from metadata_fields table

**Column Preference Endpoints (Already Existed in UserController)**
- `GET /api/users/{userId}/columns` - Get user's column preferences
- `PUT /api/users/{userId}/columns` - Update column preferences

#### Frontend Components

**metadataService.js**
- Service for metadata-related API calls
- Method: `getAllFields()` - Fetches all available column options

**ModifyColumnsDialog.js**
- React dialog component for column customization
- Displays checkboxes for all available columns
- Validates at least one column must be selected
- Saves preferences to backend

**userService.js (Enhanced)**
- Added `getUserColumns(userId)` method
- Added `updateUserColumns(userId, columns)` method

### Design Patterns Used

1. **Service Layer Pattern**: metadataService encapsulates API calls
2. **DTO Pattern**: ColumnPreferenceDTO for data transfer
3. **Repository Pattern**: MetadataFieldRepository for database access
4. **Modal Dialog Pattern**: Overlay with container for UI interaction

### Limitations

- All columns treated as "STANDARD" type in frontend (could be enhanced to distinguish STANDARD vs EXIF vs CUSTOM)
- No drag-and-drop column reordering (uses displayOrder field)
- Column visibility is binary (shown/hidden) - no column width customization
- Changes require page refresh to take effect in photo table

### Expectations

- User must be authenticated to access preferences
- At least one column must remain selected (enforced by validation)
- Column preferences persist per user in user_column_preferences table
- New custom metadata fields automatically appear in available columns list

### Security Considerations

- TEMPORARY: Authentication disabled for /api/** endpoints during development
- TODO: Re-enable authentication when OAuth fully integrated
- Users can only modify their own column preferences
- Admins can modify any user's preferences

### Testing

**Backend Tests (MetadataControllerTest.java)**
- 5 JUnit tests verifying endpoint functionality
- Tests verify standard columns, EXIF fields, and custom fields included
- Tests verify new fields appear when added to database
- All tests use @ActiveProfiles("test") for test database

### Integration Notes

- Dialog component ready for integration into Photos page
- Requires Photos page to pass userId prop to dialog
- Dialog should be triggered by "Modify Columns" button above photo table
- On save, photo table should refresh with new column configuration

## Step 9: User Access Dialog

### Functionality Created
**Photo Permission Management System**

Allows photo owners to control which users can access their private photos through a dialog interface.

### Implementation Details

#### Backend Components

**PhotoPermissionController.java**
- REST controller for managing photo permissions
- Located in: `PhotoSortServices/src/main/java/com/photoSort/controller/PhotoPermissionController.java`
- Endpoints:
  - `GET /api/photos/{photoId}/permissions` - Get list of user IDs who have access to a photo
  - `PUT /api/photos/{photoId}/permissions` - Update photo permissions (replaces all existing permissions)

**Key Implementation Details**:
- Uses `@Transactional` annotation for atomic permission updates
- Uses `EntityManager.flush()` to ensure deletes are committed before inserts
- Prevents unique constraint violations on (photo_id, user_id) composite key
- Returns 404 if photo doesn't exist
- Silently skips invalid user IDs in the request

**PhotoPermissionRepository.java**
- Custom query method: `List<PhotoPermission> findByPhoto(Photo photo)`
- Efficiently retrieves all permissions for a specific photo

#### Frontend Components

**photoService.js (Enhanced)**
- Added `getPhotoPermissions(photoId)` method
- Added `updatePhotoPermissions(photoId, userIds)` method
- Located in: `photosort-frontend/src/services/photoService.js`

**UserAccessDialog.js**
- React dialog component for managing photo access
- Located in: `photosort-frontend/src/components/UserAccessDialog.js`
- Features:
  - Displays all users with checkboxes
  - Shows currently granted permissions as checked
  - Allows toggling permissions on/off
  - Save/Cancel buttons for committing changes
  - Error handling for API failures
  - Loading states during data fetch and save operations

**UserAccessDialog.css**
- Styling for the dialog component
- Uses application color scheme (Burgundy, Navy Blue, Cream)
- Responsive design with scrollable user list
- Located in: `photosort-frontend/src/styles/UserAccessDialog.css`

### Design Patterns Used

1. **Repository Pattern**: PhotoPermissionRepository for database access
2. **Service Layer Pattern**: photoService encapsulates API calls
3. **Modal Dialog Pattern**: Overlay with centered dialog for UI interaction
4. **Optimistic UI**: Local state updates before API confirmation
5. **Transaction Management**: @Transactional with explicit flush for data consistency

### Limitations

- No pagination for user list (could be slow with many users)
- Cannot exclude specific users from being shown in the list
- No search/filter functionality for finding users
- All permissions replaced on update (no partial updates)
- No permission history or audit trail
- Changes are immediate (no undo functionality)

### Expectations

- Photo must exist in database before permissions can be managed
- User IDs in permission list must be valid (invalid IDs are silently skipped)
- Updates replace ALL existing permissions atomically
- Empty permission list removes all access
- Dialog expects userService.getAllUsers() to return all users
- Component expects props: photoId, photoFilename, onClose, onSave

### Security Considerations

- TEMPORARY: Authentication disabled for /api/** endpoints during development
- TODO: Add authorization check to verify requester is photo owner or admin
- TODO: Re-enable CSRF protection for permission endpoints
- Currently any authenticated user can modify any photo's permissions
- No rate limiting on permission updates

### Testing

**Backend Tests (PhotoPermissionControllerTest.java)**
- 7 JUnit tests with Spring Boot Test
- Located in: `PhotoSortServices/src/test/java/com/photoSort/controller/PhotoPermissionControllerTest.java`
- Test coverage:
  - Get permissions returns users with access
  - Get permissions for photo with no permissions (returns empty list)
  - Update permissions adds new users
  - Update permissions removes users
  - Update permissions replaces existing users
  - Get permissions for non-existent photo returns 404
  - Update permissions for non-existent photo returns 404

**Frontend Tests (UserAccessDialog.test.js)**
- 13 Jest/React Testing Library tests
- Located in: `photosort-frontend/src/components/UserAccessDialog.test.js`
- Test coverage:
  - Renders with photo filename
  - Displays loading state
  - Displays all users after loading
  - Shows checked checkboxes for users with access
  - Can toggle user permissions
  - Save button updates permissions and closes dialog
  - Cancel button closes without saving
  - Error handling for user load failures
  - Error handling for permission load failures
  - Error handling for save failures
  - No users message when list is empty
  - Save button shows "Saving..." state
  - Buttons disabled while saving

### Integration Notes

- Dialog component ready for integration into Photos page
- Requires props: photoId, photoFilename, onClose callback, onSave callback
- Should be triggered by "Manage Access" button on private photos
- Can be used even before Photos page is fully implemented
- Integrates with existing userService.getAllUsers() method

### Database Schema

**photo_permissions table** (created in Step 1):
- `permission_id` (PK) - Auto-generated ID
- `photo_id` (FK) - References photos table
- `user_id` (FK) - References users table
- `created_at` - Auto-set timestamp
- Unique constraint on (photo_id, user_id)

### Performance Considerations

- Uses repository method `findByPhoto()` instead of `findAll().filter()`
- Bulk delete + bulk insert pattern for permission updates
- Explicit flush() prevents constraint violations during transaction
- No N+1 query problems (single query per operation)

### Future Enhancements

- Add pagination for large user lists
- Add search/filter for finding users
- Add permission history/audit trail
- Add undo functionality
- Add authorization checks (owner/admin only)
- Add incremental permission updates (add/remove specific users)
- Show user profile pictures in the list
- Add "Select All" / "Deselect All" buttons


## Step 10: Image Display Page

### Functionality Created
**Photo Detail View with Metadata and Tag Editing**

Complete photo viewing experience with full-resolution image display, EXIF data presentation, and inline editing of custom metadata and tags.

### Implementation Details

#### Backend Components

**PhotoDetailDTO.java** - Comprehensive DTO with all photo information including nested exifData, metadata list, and tags list

**PhotoDetailController.java** - 4 endpoints:
- `GET /api/photos/{id}` - Complete photo details
- `GET /api/photos/{id}/image` - Image file from disk  
- `PUT /api/photos/{id}/metadata` - Update custom metadata
- `PUT /api/photos/{id}/tags` - Update tags

Key features: Image serving from disk (not database), auto-creates metadata fields/tags, uses @Transactional with flush()

#### Frontend Components

**ImageDisplay.js** (Main Page) - Two-column layout (70%/30%), route `/photo/:photoId`

**ExifDataSection.js** - Read-only EXIF display, GPS as Google Maps link

**CustomMetadataSection.js** - Inline editing, add/delete fields, "Add Field" modal

**TagsSection.js** - Tag chips with delete, text input for new tags

**AddMetadataFieldModal.js** - Modal for adding metadata fields

**ImageDisplay.css** - Burgundy/Navy/Cream colors, responsive design

### Testing

**Backend**: 12 JUnit tests - All pass (71 total tests, zero regressions)

**Frontend**: 17 Jest/React Testing Library tests - All pass
- ImageDisplay.test.js (7 tests)
- CustomMetadataSection.test.js (4 tests)
- TagsSection.test.js (6 tests)

### Key Technical Details

- Images served from disk via InputStreamResource
- @JsonProperty annotation for fNumber field serialization
- EntityManager.flush() prevents unique constraint violations
- Inline editing with save on Enter key
- Tag/metadata updates replace all (not incremental)



## Step 11: Scripts Table Page

### Functionality Created
**Script Management Table (Admin Only)**

Admin page for viewing and managing automated scripts with table display showing script name, file name, schedule type, and file extension.

### Implementation Details

#### Backend Components

**ScriptController.java** - 5 REST endpoints:
- `GET /api/scripts` - Get all scripts
- `GET /api/scripts/{id}` - Get single script  
- `POST /api/scripts` - Create new script
- `PUT /api/scripts/{id}` - Update existing script
- `DELETE /api/scripts/{id}` - Delete script

Uses ApiResponse wrapper pattern, returns 404 for non-existent IDs, clears ID on POST to ensure new entity

Note: Currently returns all scripts without server-side pagination (client-side pagination implemented)

#### Frontend Components

**Scripts.js** (Main Page) - Reuses existing component architecture: TablePage, SearchControls, PaginationControls, useTableData hook

**ScriptTable.js** - Domain-specific table wrapper using DataTable, columns: Script Name, Script File, Schedule, File Extension

**scriptService.js** - API methods: getAllScripts, getScriptById, createScript, updateScript, deleteScript

**ScriptTable.css** - Schedule badges (daily/periodic/manual), file extension code styling

Key architectural pattern: Client-side pagination wrapper adapts simple list API response to PagedResponse format expected by useTableData hook

#### UI Features

- Schedule display: "Daily at HH:MM" or "Every X minutes/hours/days"
- File extension displayed as code blocks
- "Add Script" button (placeholder for Step 12)
- "Edit" buttons per row (placeholder for Step 12)
- Search, sort, pagination (client-side)

### Testing

**Backend**: 10 JUnit tests in ScriptControllerTest.java - All pass (81 total tests, zero regressions)
- CRUD operations for scripts
- 404 handling for non-existent IDs
- runTime vs periodicityMinutes validation

**Frontend**: 18 Jest tests in Scripts.test.js - All pass (229 total tests, zero regressions)
- Page rendering, loading, error states
- Script table display with schedule badges
- Search controls and pagination
- Add/Edit button placeholders

### Key Technical Details

- Client-side pagination: fetchScriptsWithPagination wrapper function converts backend list response to PagedResponse format
- Consistent with existing table patterns (Users, Photos)
- Reuses DataTable, useTableData hook, SearchControls, PaginationControls
- Schedule formatting: periodicityMinutes displayed as human-readable intervals
- RunTime formatted as HH:MM (removes seconds)

### Database Schema

**scripts table** (created in Step 1):
- `script_id` (PK) - Auto-generated
- `script_name` - Unique name
- `script_file_name` - Script filename
- `script_contents` - Full script text
- `run_time` - Daily execution time (mutually exclusive with periodicity)
- `periodicity_minutes` - Recurring interval in minutes
- `file_extension` - File type to process
- `created_at`, `updated_at` - Timestamps

### Limitations

- Edit Script Dialog not implemented (Step 12)
- Server-side pagination not implemented (client-side only)
- Admin-only access not enforced in backend (placeholder)
- No script execution functionality (Step 14)
- No validation for runTime vs periodicityMinutes mutual exclusivity

### Future Enhancements (Step 12)

- Edit Script Dialog with code editor
- Script file upload
- runTime/Periodicity validation  
- Script engine reload trigger
- Syntax highlighting for script contents
