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

---

## Step 12: Edit Script Dialog (Admin Only)

**Completed**: 2025-12-03

### Functionality Created

Dialog component for creating and editing automated scripts. Provides a form-based interface for managing script metadata, scheduling configuration, and script contents.

### Implementation

**Frontend Components**:

1. **EditScriptDialog.js** - Main dialog component
   - Create mode (script=null) vs Edit mode (script provided)
   - Form fields: Script Name, Script File Name, Run Time, Periodicity, File Extension, Script Contents
   - Mutual exclusivity: Run Time and Periodicity cannot both be set
   - Validation: Script name is required
   - Actions: Save (create/update), Delete (edit only), Cancel
   - State management: form fields, loading, saving, error, validation states

2. **EditScriptDialog.css** - Dialog styles
   - Burgundy/cream color scheme consistent with application
   - Form layout with grid for Run Time/Periodicity row
   - Monospace font for script contents textarea
   - Responsive design for mobile

3. **Scripts.js** - Updated to integrate dialog
   - Added state: showEditDialog, selectedScript
   - handleAddScript() - Opens dialog with null script
   - handleEdit(script) - Opens dialog with selected script
   - handleCloseDialog() - Closes dialog and resets state
   - handleDialogSave() - Refreshes table after save/delete
   - Conditional rendering of EditScriptDialog component

**Backend**: No changes required - Uses existing ScriptController endpoints from Step 11:
- POST /api/scripts - Create script
- PUT /api/scripts/{id} - Update script
- DELETE /api/scripts/{id} - Delete script

### Testing

**Frontend**: 20 Jest tests in EditScriptDialog.test.js - All pass (249 total tests, zero regressions)
- Dialog rendering for new/edit modes
- Form field input and state management
- Run Time/Periodicity mutual exclusivity
- Validation (empty script name)
- Save/Delete/Cancel actions
- Error handling and display
- Loading states and disabled buttons
- Periodicity dropdown options

**Scripts.test.js**: Updated 2 tests
- "opens dialog when Add Script button clicked" - Verifies dialog opens
- "opens dialog when Edit button clicked" - Verifies dialog opens with script

**Backend**: 81 JUnit tests - All pass (zero regressions)

### Key Technical Details

**Time Format Conversion**:
- HTML time input uses HH:MM format
- Backend expects HH:MM:SS format
- EditScriptDialog converts on save: `${runTime}:00`
- EditScriptDialog strips seconds on load: `timeParts[0]:timeParts[1]`

**Mutual Exclusivity Logic**:
- handleRunTimeChange() clears periodicityMinutes when runTime is set
- handlePeriodicityChange() clears runTime when periodicity is set
- Ensures only one scheduling method is active

**Periodicity Options**:
- None: '' (manual execution)
- 1 minute: 1
- 5 minutes: 5
- 10 minutes: 10
- 1 hour: 60
- 2 hours: 120
- 6 hours: 360
- 1 day: 1440

**Form Validation**:
- Script name is required (cannot be empty/whitespace)
- All other fields are optional
- Validation error displayed above form

**Delete Confirmation**:
- Uses window.confirm() for user confirmation
- Shows script name in confirmation message
- Only shown in edit mode (not for new scripts)

**Dialog Architecture**:
- Follows UserAccessDialog pattern from Step 9
- Full-screen overlay with centered dialog
- Header (burgundy), content (scrollable), footer (fixed)
- Footer split: Delete on left, Cancel/Save on right
- Buttons disabled during save operation

### Testing Summary

All tests pass with zero regressions:
- **Backend**: 81/81 tests passing
- **Frontend**: 249/249 tests passing  
- **Total**: 330/330 tests passing ✅

### Limitations

- Script file upload not implemented (manual entry only)
- Script engine reload not implemented (Step 19)
- No syntax highlighting for script contents
- No code linting or validation
- No preview/test execution capability
- Admin-only access not enforced

### Future Enhancements

- File browser/uploader for script files
- Syntax highlighting for Python/Bash code
- Code linter integration
- Script test execution button
- Script history/versioning
- Import/export scripts
- Script templates library
- Dry-run/preview capability

---

## Step 15: EXIF Data Extraction

**Functionality Created**: Automatic EXIF metadata extraction from image files when detected by Git polling service

**Implementation Notes**:

This feature integrates EXIF data extraction with the Git polling workflow established in Step 14. When the GitPollingService detects new or changed image files in the repository, it now automatically extracts EXIF metadata and creates/updates database records.

**Core Components**:

1. **ExifDataService**: Extracts EXIF metadata using metadata-extractor library
   - Camera information: make, model
   - Date/time: original capture date
   - GPS coordinates: latitude/longitude (decimal format with 8 decimal places)
   - Exposure settings: exposure time, f-number, ISO speed, focal length
   - Orientation: image orientation flag
   - Returns null if no EXIF data found (graceful handling)

2. **GitPollingService Integration**: Enhanced processImageFile() method
   - Extracts EXIF data via ExifDataService
   - Extracts image dimensions separately (belongs on Photo, not ExifData)
   - Gets file metadata: size, created date, modified date
   - Creates or updates Photo record
   - Saves associated ExifData if extraction successful
   - Handles missing EXIF data gracefully (e.g., PNG screenshots)

3. **First Poll Enhancement**: Implemented all-files processing
   - When oldCommitHash is null (first poll), TreeWalk iterates all files in repository
   - Previously returned empty list; now processes all existing image files
   - Critical for initial repository setup

**Database Schema**:
- Photo entity: stores basic file properties (dimensions, size, dates, path)
- ExifData entity: stores camera/exposure metadata with one-to-one relationship to Photo
- ExifData.photo_id has ON DELETE CASCADE for automatic cleanup

**Libraries**:
- metadata-extractor 2.19.0: EXIF extraction
- JGit: Git operations and tree walking

**Supported Image Formats**:
- JPG/JPEG: Full EXIF support
- PNG: Limited/no EXIF (handled gracefully)
- GIF, BMP, TIFF, WebP: Varies by format

**Data Extraction Details**:
- GPS coordinates: Converted from degrees/minutes/seconds to decimal format
- Date/time: Supports multiple EXIF date format patterns
- Dimensions: Extracted from JPEG directory metadata
- File attributes: Using Java NIO BasicFileAttributes

**Integration Flow**:
1. GitPollingService polls repository (5-minute interval, 60-second initial delay)
2. Detects changed/new image files via Git diff or TreeWalk (first poll)
3. For each image file:
   - Extract EXIF data
   - Extract dimensions
   - Get file attributes
   - Check if photo exists by file path
   - Create/update Photo record
   - Save ExifData if extracted

**Error Handling**:
- Gracefully handles images without EXIF data
- Logs warnings for unreadable files
- Continues processing on individual file errors
- Does not crash scheduled task on failures

### Testing Summary

All tests passing:
- **Backend**: 111/111 tests passing (10 new ExifDataService tests)
- **Integration**: Tested with 135 real photos from configured Git repository
- **EXIF Extraction**: Successfully extracted metadata from 124/135 photos (11 PNG files without EXIF)
- **Total**: 111 backend tests ✅

### Limitations

- Deprecated BigDecimal.ROUND_HALF_UP used (should migrate to RoundingMode.HALF_UP)
- No support for XMP or IPTC metadata (only EXIF)
- Image dimensions only extracted from JPEG directory (may fail for some formats)
- No thumbnail generation (planned for future step)
- Photos created without owner (must be assigned by admin later)

### Expectations

- Git repository must be configured in application.properties (git.repo.path)
- Repository must be initialized and contain .git directory
- File paths must be absolute and accessible to application
- Database schema must match entity definitions
- GitPollingService runs automatically after application startup (60-second delay)
- Subsequent polls occur every 5 minutes (configurable via git.poll.interval.minutes)

---

## Step 16: Metadata File Parsing

**Functionality Created**: Parse optional .metadata files containing custom metadata for photos

**Implementation Notes**:

This service reads simple key=value format files that can accompany photos to provide custom metadata. The parser handles a special "tags" field differently (comma-separated list) and creates regular string fields for all other metadata.

**Core Components**:

1. **MetadataParserService**: Parses .metadata files
   - Reads file line by line
   - Parses key=value format (splits on first '=' to allow '=' in values)
   - Special handling for "tags" field: splits on comma, trims whitespace, returns List<String>
   - Regular fields returned as String values
   - Returns Map<String, Object> where values are either String or List<String>

**File Format**:
```
Title=Family Vacation 2024
Location=Grand Canyon, Arizona
tags=vacation,family,nature,landscape
Event=Summer Trip
People=John,Jane,Kids
```

**Parsing Rules**:
- Each line: `fieldname=value`
- Empty lines skipped
- Lines without '=' skipped with warning
- Lines with empty keys (e.g., `=value`) skipped with warning
- Duplicate keys: last value wins
- Empty values: stored as empty string
- Special characters and Unicode: preserved as-is
- Only splits on first '=' (allows '=' in values like `Formula=E=mc^2`)

**Tags Field Special Handling**:
- Comma-separated values split into list
- Each tag trimmed of leading/trailing whitespace
- Empty tags removed
- Returned as `List<String>` instead of String

**Error Handling**:
- Missing file: returns empty map (not an error)
- Malformed lines: logged as warning, line skipped
- IO errors: logged as error, returns partial results or empty map

**Integration Points**:
- Will be called from PhotoProcessingService (Step 18)
- Results stored in metadata_fields and photo_metadata tables
- Tags extracted and stored in tags and photo_tags tables

### Testing Summary

All tests passing:
- **Backend**: 121/121 tests passing (10 new MetadataParserService tests)
- **Test Coverage**: All parsing scenarios including Unicode, special characters, malformed data
- **Total**: 121 backend tests ✅

**Test Cases Verified**:
1. Well-formatted .metadata files parsed correctly
2. Tags field parsed as comma-separated list
3. Tags with extra whitespace trimmed properly
4. Missing files return empty map (graceful handling)
5. Malformed lines skipped with warnings
6. Duplicate keys: last value wins
7. Empty values stored as empty strings
8. Special characters preserved (@, #, :, !, ?, \)
9. Unicode/international characters supported (Chinese, Portuguese, French)
10. Equals sign in values handled correctly

### Limitations

- No support for multi-line values
- No support for escaped characters or quotes
- No support for comments in .metadata files
- File must use UTF-8 encoding for international characters
- Case-sensitive field names (Title ≠ title)

### Expectations

- .metadata files must be in same directory as photo
- Files should use UTF-8 encoding
- One key=value pair per line
- Special "tags" field for comma-separated tags
- Service is stateless - no caching of parsed results

---

## Step 17: STAG Script Integration

**Functionality Created**: Execute STAG Python script for AI-generated photo tagging

**Implementation Notes**:

This service integrates with the STAG (Simple Tag Auto-Generator) Python script to automatically generate descriptive tags for photos using AI/ML analysis. The service executes the external Python script, captures output, and parses generated tags.

**Core Components**:

1. **StagService**: Executes STAG script and parses results
   - Uses ProcessBuilder for external process execution
   - Configures working directory to script location
   - Implements 30-second timeout to prevent hanging
   - Captures stdout/stderr output
   - Parses tags from output (supports comma-separated or newline-separated)
   - Returns empty list on errors (graceful degradation)

**Configuration**:
```properties
stag.script.path=./stag-main/stag.py
stag.python.executable=python3
```

**Process Execution Flow**:
1. Validate input photo file
2. Read script path and Python executable from configuration
3. Check if script exists (warn if missing, return empty list)
4. Build ProcessBuilder with: `python3 stag.py <photo_path>`
5. Set working directory to script directory
6. Redirect error stream to output stream
7. Start process and capture output
8. Wait for completion with 30-second timeout
9. Parse output into tag list
10. Return tags or empty list on error

**Output Parsing**:
- **Comma-separated**: `outdoor,nature,landscape,sunset`
- **Newline-separated**:
  ```
  outdoor
  nature
  landscape
  sunset
  ```
- Automatically detects format (tries comma-separated first)
- Trims whitespace from each tag
- Removes empty tags

**Error Handling**:
- Missing photo file: returns empty list, logs warning
- Script not found: returns empty list, logs warning (allows system to run without STAG)
- Python not installed: returns empty list, logs error
- Script crashes: returns empty list, logs error with output
- Timeout: destroys process forcibly, returns empty list
- Non-zero exit code: returns empty list, logs warning with output

**Timeout Configuration**:
- Default: 30 seconds
- Process destroyed forcibly if timeout exceeded
- Prevents hanging on problematic images or script issues

**Integration Points**:
- Called from PhotoProcessingService (Step 18)
- Generated tags stored in tags and photo_tags tables
- Executed for each new/changed photo during Git polling

### Testing Summary

All tests passing:
- **Backend**: 131/131 tests passing (10 new StagService tests)
- **Graceful degradation**: Tests pass whether STAG script is installed or not
- **Total**: 131 backend tests ✅

**Test Cases Verified**:
1. Service creation and dependency injection
2. Null/invalid file handling (returns empty list)
3. Timeout handling (doesn't hang)
4. Configuration access verified
5. Script not found handled gracefully
6. Special characters in file paths supported
7. Comma-separated tag parsing structure
8. Newline-separated tag parsing structure
9. Empty output handled gracefully
10. Concurrent calls don't cause issues

### Limitations

- External dependency on STAG Python script
- Requires Python 3 to be installed
- 30-second timeout may be too short for very large images
- No batch processing support (one image at a time)
- Output format must be comma-separated or newline-separated
- No support for JSON or structured output formats
- Script errors result in empty tag list (no partial results)

### Expectations

- STAG Python script must be at configured path
- Python 3 must be installed and accessible
- Script should accept image file path as single argument
- Script should output tags to stdout
- Script should exit with code 0 on success
- Script should complete within 30 seconds
- If script not available, system continues without AI tags (graceful degradation)


---

## Step 18: Photo Processing Pipeline

**Functionality Created**: Orchestrator service that processes photos through complete pipeline integrating all previous services

This service acts as the central orchestrator for photo processing, coordinating EXIF extraction, metadata parsing, STAG tagging, thumbnail generation, and database persistence. It ensures atomic transactions and handles partial failures gracefully.

**Core Components**:

1. **PhotoProcessingService**: Main orchestrator service
   - Coordinates all photo processing steps
   - Uses @Transactional for atomic database operations
   - Handles duplicate photo detection
   - Processes EXIF data, metadata files, and STAG tags
   - Manages Tag and PhotoTag associations
   - Logs script execution
   - Provides graceful degradation on partial failures

2. **GitPollingService Integration**: Refactored to use PhotoProcessingService
   - Simplified from direct database manipulation to single method call
   - Removed duplicate logic (EXIF extraction, photo record creation)
   - Now delegates all processing to PhotoProcessingService

**Processing Pipeline**:

1. **Determine Owner**: Look up user by commit author email, fallback to first admin
2. **Duplicate Detection**: Query Photo by file_path - update if exists, create if new
3. **Extract File Metadata**: File size, creation date, modification date using BasicFileAttributes
4. **Extract Image Dimensions**: Use metadata-extractor JpegDirectory for width/height
5. **Generate Thumbnail**: Placeholder (returns null for now, to be implemented in Step 20)
6. **Save Photo Record**: Create or update with all attributes
7. **Extract EXIF Data**: Call ExifDataService.extractExifData()
8. **Parse Metadata File**: Check for .metadata file, call MetadataParserService if exists
9. **Generate STAG Tags**: Call StagService.generateTags()
10. **Execute Custom Scripts**: Placeholder for Step 19 (not yet implemented)
11. **Save Tags**: Find or create Tag records, create PhotoTag associations
12. **Log Execution**: Create ScriptExecutionLog entries for STAG script

**Transaction Management**:

The processPhoto() method uses @Transactional to ensure:
- All database operations succeed or rollback together
- No partial state left in database on errors
- Automatic rollback on uncaught exceptions
- Proper foreign key constraint handling

**Error Handling Strategy**:

- **EXIF Extraction Failure**: Log warning, continue processing (EXIF is optional)
- **Metadata File Missing**: Continue processing (metadata files are optional)
- **STAG Script Failure**: Log warning, create failure log entry, continue processing
- **Database Constraint Violation**: Transaction rollback, exception propagated
- **File I/O Errors**: Exception propagated, transaction rollback

**Helper Methods**:

1. **findOrCreateUser(email)**: Looks up user by email, returns first admin if not found
2. **findOrCreateTag(tagValue)**: Queries TagRepository, creates new tag if not exists
3. **findOrCreateMetadataField(fieldName)**: Queries MetadataFieldRepository, creates if not exists
4. **extractImageDimensions(file)**: Uses metadata-extractor JpegDirectory for dimensions
5. **generateThumbnail(file)**: Placeholder returning null (Step 20)
6. **hasExifData(exifData)**: Checks if ExifData has meaningful data
7. **copyExifData(source, dest)**: Copies EXIF fields when updating existing record
8. **processExifData(file, photo)**: Extract and save/update EXIF data
9. **processMetadataFile(file, photo)**: Parse and save metadata fields and tags
10. **processStagTags(file, photo)**: Generate and save STAG tags
11. **processCustomScripts(file, photo)**: Placeholder for Step 19
12. **createPhotoTagAssociation(photo, tag)**: Create PhotoTag if doesn't exist
13. **logScriptExecution(photo, scriptName, status, error)**: Log to script_execution_log

**Repository Methods Used**:

- PhotoRepository: findByFilePath(), save()
- ExifDataRepository: findByPhoto(), save()
- PhotoMetadataRepository: findByPhotoAndField(), save()
- TagRepository: findByTagValue(), save()
- PhotoTagRepository: findByPhotoAndTag(), save()
- MetadataFieldRepository: findByFieldName(), save()
- ScriptExecutionLogRepository: save()
- UserRepository: findByEmail(), findAll()
- ScriptRepository: findByScriptName()

**Database Operations**:

1. **Photo Record**: Created or updated (findByFilePath determines which)
2. **ExifData Record**: Created or updated (findByPhoto determines which)
3. **PhotoMetadata Records**: Multiple records, one per metadata field
4. **Tag Records**: Find existing or create new (prevents duplicates)
5. **PhotoTag Associations**: Create only if doesn't exist (findByPhotoAndTag prevents duplicates)
6. **MetadataField Records**: Find existing or create new (prevents duplicates)
7. **ScriptExecutionLog**: Created for STAG script execution

### Testing Summary

All tests passing:
- **Backend**: 144/144 tests passing (13 new PhotoProcessingService tests + 131 existing)
- **Complete Pipeline**: All components integrated and tested
- **Total**: 144 backend tests ✅

**Test Cases Verified**:
1. Service creation and dependency injection
2. Complete pipeline with EXIF, metadata file, and STAG tags
3. Photo record created with correct attributes
4. EXIF data saved correctly
5. Custom metadata saved correctly
6. STAG tags saved correctly
7. Metadata file tags saved correctly
8. Thumbnail generation integrated (placeholder)
9. Photo without EXIF data handled gracefully
10. Photo without metadata file handled gracefully
11. STAG script failure handled gracefully (continue processing)
12. Transaction rollback structure verified
13. Duplicate photo detection working (same file_path)
14. Execution logging integrated

### Limitations

- **Thumbnail Generation**: Placeholder (returns null) - will be implemented in Step 20
- **Custom Script Execution**: Placeholder - will be implemented in Step 19
- **Commit Author**: Currently passes null (defaults to first admin) - could be enhanced to extract from Git commit
- **Batch Processing**: Processes one photo at a time (no parallel processing)
- **Progress Tracking**: No progress callbacks or status updates during processing
- **Partial Rollback**: @Transactional rolls back entire operation on error (all-or-nothing)

### Expectations

- User with ADMIN role must exist in database (getDefaultAdmin requires it)
- All referenced services must be available (ExifDataService, MetadataParserService, StagService)
- Photo file must exist and be readable
- File system access required for reading photos and .metadata files
- Database must support transactions (PostgreSQL)
- Unique constraint on photos.file_path enforced by database
- Unique constraint on tags.tag_value enforced by database
- Unique constraint on metadata_fields.field_name enforced by database
- Optional components (EXIF, metadata files, STAG) degrade gracefully when unavailable

---

## Step 19: Custom Script Execution Engine

### Functionality Created

**ScriptExecutionService** - Service for executing custom scripts based on file extensions, with support for daily and periodic scheduling (Step 19 of PhotoSort specification).

### Implementation Notes

**Architecture**:
- **@Service annotation**: Spring-managed service with dependency injection
- **@PostConstruct init()**: Loads scripts from database on application startup
- **@Scheduled methods**: Two scheduled methods for daily and periodic script execution
- **ProcessBuilder**: Java process execution with timeout and output capture
- **In-memory mapping**: ConcurrentHashMap for thread-safe file extension → script mapping
- **Execution tracking**: LocalDateTime tracking for periodic script last-execution time

**Design Patterns**:
- **Service Layer Pattern**: Business logic separated from controllers
- **Scheduled Task Pattern**: @Scheduled annotation for time-based execution
- **Template Method Pattern**: executeScript() and executeScheduledScript() share common logic
- **Strategy Pattern**: Different execution strategies (file-based, scheduled daily, scheduled periodic)
- **Resource Management**: Temp files with automatic cleanup in finally blocks

**Key Components**:

1. **Script Loading** (ScriptExecutionService.java:61-106):
   - @PostConstruct init() method loads scripts on application startup
   - loadScripts() queries database for all Script records
   - Extension-based scripts added to extensionToScriptMap (ConcurrentHashMap)
   - Daily and periodic scripts counted and logged

2. **File Extension Mapping** (ScriptExecutionService.java:53-120):
   - ConcurrentHashMap<String, Script> for thread-safe access
   - Key: file extension (e.g., ".jpg", ".png")
   - Value: Script entity
   - getScriptForExtension() provides lookup by extension

3. **Script Execution** (ScriptExecutionService.java:130-238):
   - executeScript(Script, File, Photo) - Execute script for a specific file
   - Creates temp script file from scriptContents or uses scriptFileName
   - Uses ProcessBuilder with "/bin/bash" to execute script
   - **CRITICAL**: Calls waitFor() with timeout BEFORE reading output (prevents blocking)
   - Sets working directory to file's parent directory
   - 60-second timeout prevents hanging scripts
   - Captures stdout/stderr (redirectErrorStream)
   - Returns boolean success status
   - Logs execution to script_execution_log table

4. **Daily Scheduled Scripts** (ScriptExecutionService.java:243-262):
   - @Scheduled(fixedDelay = 60000) - Runs every minute
   - Queries scripts with runTime != null
   - Checks if current time matches configured runTime (within 1 minute)
   - Executes matching scripts via executeScheduledScript()

5. **Periodic Scheduled Scripts** (ScriptExecutionService.java:267-293):
   - @Scheduled(fixedDelay = 60000) - Runs every minute
   - Queries scripts with periodicityMinutes != null
   - Tracks last execution time in lastExecutionMap (ConcurrentHashMap)
   - Executes if never run or if periodicityMinutes has elapsed
   - Updates lastExecutionMap after execution

6. **Execution Logging** (ScriptExecutionService.java:392-404):
   - logExecution(Script, Photo, success, errorMessage)
   - Creates ScriptExecutionLog record for every execution
   - Records SUCCESS or FAILURE status
   - Captures error messages for failed executions
   - Gracefully handles logging failures (warns but continues)

**Integration with PhotoProcessingService** (PhotoProcessingService.java:314-341):
- processCustomScripts() called as part of photo processing pipeline
- Extracts file extension from photo file name
- Looks up script from ScriptExecutionService.getScriptForExtension()
- Executes script if found, logs if not found
- Failures logged but don't stop photo processing (graceful degradation)

**Timeout Implementation (CRITICAL BUG FIX)**:

The original implementation had a critical bug where reading the output stream BEFORE calling waitFor() caused the thread to block indefinitely. The fix:

```java
// WRONG (blocks indefinitely):
Process process = processBuilder.start();
// Reading output blocks until process completes
BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
while ((line = reader.readLine()) != null) { ... }
boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS); // Never reached

// CORRECT (timeout works):
Process process = processBuilder.start();
// Wait for completion FIRST (with timeout)
boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
if (!completed) {
    process.destroyForcibly();
    return false;
}
// THEN read output after process has completed
BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
while ((line = reader.readLine()) != null) { ... }
```

**Repository Methods Used**:
- ScriptRepository.findAll() - Load all scripts
- ScriptRepository.findByRunTimeIsNotNull() - Get daily scheduled scripts
- ScriptRepository.findByPeriodicityMinutesIsNotNull() - Get periodic scripts
- ScriptExecutionLogRepository.save() - Log script executions
- PhotoRepository (via PhotoProcessingService) - Access photo records

**Database Tables**:
- **scripts**: Source of script configuration
- **script_execution_log**: Execution history and status
- **photos**: Context for file-based script execution

### Testing Summary

All tests passing:
- **Backend**: 156/156 tests passing (12 new ScriptExecutionService tests + 144 existing)
- **Script Execution**: All timeout, scheduling, and logging tests passing
- **Total**: 156 backend tests ✅

**Test Cases Verified** (ScriptExecutionServiceTest.java):
1. Service creation with scripts loaded from database on startup
2. Correct script selected based on file extension
3. Script executes successfully with valid script contents
4. Script output captured (verified internally)
5. Daily scheduled scripts structure (requires running application)
6. Periodic scripts structure (requires running application)
7. Execution logged to script_execution_log table
8. Failures logged with error message
9. reloadScripts() refreshes in-memory map from database
10. Multiple scripts can run concurrently without interference
11. **Script timeout prevents hanging** (sleep 1000s times out in ~60s) ✅
12. Scripts execute with correct working directory (file's parent)

### Limitations

- **Shell Dependency**: Requires /bin/bash (Linux/Unix only, not Windows compatible)
- **Script Language**: Only bash scripts supported (could extend to Python, Ruby, etc.)
- **Output Capture**: Reads output AFTER execution completes (no streaming/real-time output)
- **Timeout Value**: Hard-coded to 60 seconds (not configurable per script)
- **Scheduling Granularity**: Checks every 60 seconds (1-minute resolution, not cron-like precision)
- **Daily Script Window**: Executes if within 1 minute of runTime (may miss if system busy)
- **Periodic Script Drift**: Uses elapsed time, not absolute schedule (can drift over time)
- **No Script Queuing**: Concurrent executions allowed (no queue or rate limiting)
- **No Script Chaining**: Scripts execute independently (no dependencies or ordering)
- **Error Recovery**: Failed scripts logged but not retried
- **Process Tree Cleanup**: destroyForcibly() may not kill all child processes on some systems
- **Temp File Security**: Temp script files readable by all users (no permission restrictions)

### Expectations

- PostgreSQL database with scripts and script_execution_log tables
- /bin/bash available on system (Linux/Unix environment)
- File system write access for temp script files (/tmp directory)
- Scripts must be valid bash syntax
- Scripts exit with code 0 for success, non-zero for failure
- Scripts must complete within 60 seconds or will be killed
- Daily scripts run approximately once per day (within 1-minute window of runTime)
- Periodic scripts run at configured intervals (approximately, not exactly)
- Scripts run with working directory set to file's parent (for file-based execution)
- Scripts receive file path as first argument (for file-based execution)
- Scheduled scripts run with no arguments (must be self-contained)
- Application must remain running for scheduled scripts to execute
- ConcurrentHashMap provides thread safety for multi-threaded access
- Script execution failures don't stop photo processing pipeline
- All script executions logged (success and failure)
- Execution logs retained indefinitely (no automatic cleanup)
- Database must support @Transactional for execution logging

### Photo Processing Pipeline Integration

PhotoProcessingService.processPhoto() calls processCustomScripts() which:
1. Extracts file extension from photo file name
2. Looks up script via ScriptExecutionService.getScriptForExtension()
3. If script found, executes it with executeScript(script, photoFile, photo)
4. Script execution logged to script_execution_log
5. Failures logged but processing continues (graceful degradation)

This allows custom processing logic per file type without modifying core code.

---

## Step 20: Thumbnail Generation

### Functionality Created

**ThumbnailService** - Service for generating 200x200px thumbnails for photo files with aspect ratio maintained (Step 20 of PhotoSort specification).

### Implementation Notes

**Architecture**:
- **@Service annotation**: Spring-managed service with dependency injection
- **Thumbnailator library**: net.coobird:thumbnailator:0.4.20 for image resizing
- **ConfigService integration**: Uses git.repo.path to determine thumbnail directory
- **Graceful failure**: Returns null on errors, logs warnings/errors

**Design Patterns**:
- **Service Layer Pattern**: Business logic separated from controllers
- **Dependency Injection**: ConfigService autowired for configuration
- **Fail-Safe Pattern**: Null returns instead of exceptions for invalid inputs
- **Resource Management**: Automatic directory creation if doesn't exist

**Key Components**:

1. **Thumbnail Generation** (ThumbnailService.java:39-99):
   - generateThumbnail(File photoFile) - Main entry point
   - Returns absolute path to generated thumbnail, or null on failure
   - Validates file exists and is readable
   - Gets thumbnail directory from ConfigService (git.repo.path/thumbnails)
   - Creates thumbnails directory if needed (Files.createDirectories)
   - Extracts filename and extension from original file
   - Generates thumbnail filename: {original_name}_thumb.{ext}
   - Uses Thumbnailator to resize to max 200x200px
   - Maintains aspect ratio automatically
   - Sets JPEG quality to 0.85 for file size control

2. **Thumbnailator Usage** (ThumbnailService.java:82-86):
   ```java
   Thumbnails.of(photoFile)
       .size(MAX_THUMBNAIL_WIDTH, MAX_THUMBNAIL_HEIGHT)  // 200x200 max
       .outputQuality(JPEG_QUALITY)                      // 0.85 quality
       .toFile(thumbnailFile);
   ```
   - Automatically maintains aspect ratio (image fits within 200x200 box)
   - Preserves original format (JPG → JPG, PNG → PNG)
   - Quality setting only affects JPEG output

3. **Directory Management** (ThumbnailService.java:48-62):
   - Reads git.repo.path from ConfigService
   - Defaults to temp directory if not configured
   - Creates {git.repo.path}/thumbnails directory
   - Uses Files.createDirectories() for recursive creation
   - Logs directory creation and failures

4. **Error Handling** (ThumbnailService.java:41-45, 90-96):
   - Null/non-existent file: Logs warning, returns null
   - Directory creation failure: Logs error, returns null
   - Image read failure (corrupt/unsupported): Logs warning, returns null
   - Unexpected exceptions: Logs error with stack trace, returns null
   - Never throws exceptions to calling code

5. **Filename Generation** (ThumbnailService.java:64-78):
   - Original: `photo.jpg` → Thumbnail: `photo_thumb.jpg`
   - Original: `image.png` → Thumbnail: `image_thumb.png`
   - Original: `noext` → Thumbnail: `noext_thumb`
   - Uses lastIndexOf('.') to find extension
   - Handles files without extensions

**Integration with PhotoProcessingService** (PhotoProcessingService.java:75-76, 125-127):
- @Autowired ThumbnailService injected
- Called at Step 5 of photo processing pipeline (BEFORE photo save)
- thumbnailService.generateThumbnail(photoFile) replaces placeholder
- Thumbnail path saved to photos.thumbnail_path column
- Failures don't stop photo processing (path will be null)

**Thumbnailator Library**:
- **Version**: 0.4.20 (net.coobird:thumbnailator)
- **Purpose**: Simplifies image resizing with quality control
- **Key Features**:
  - Automatic aspect ratio maintenance (proportional scaling)
  - Quality control for JPEG compression (0.0-1.0 scale)
  - Format preservation (auto-detects from file extension)
  - Built on Java ImageIO (compatible with standard image formats)
  - Thread-safe operation

### Testing Summary

All tests passing:
- **Backend**: 166/166 tests passing (10 new ThumbnailService tests + 156 existing)
- **Complete Integration**: Thumbnail generation working in photo processing pipeline
- **Total**: 166 backend tests ✅

**Test Cases Verified** (ThumbnailServiceTest.java):
1. Thumbnail generated for JPG image - verifies file exists
2. Thumbnail generated for PNG image - verifies format preserved
3. Thumbnail size max 200x200px - reads image, checks dimensions
4. Aspect ratio maintained - tests wide (4:1) and tall (1:4) images
5. Thumbnail saved to correct path - verifies "thumbnails" directory and "_thumb" suffix
6. Thumbnail path returned correctly - verifies non-null, non-empty, has extension
7. Corrupt image file handled gracefully - returns null for non-image data
8. Unsupported format handled gracefully - returns null for .xyz files
9. Thumbnail quality acceptable - verifies valid readable image with positive dimensions
10. File size reasonable < 50KB - checks file size within expected range

### Limitations

- **Naming Convention**: Uses original filename + "_thumb" instead of photo_id (photo_id not available when thumbnail created)
- **No Overwrite Protection**: Regenerating thumbnail for same file overwrites previous thumbnail
- **Directory Configuration**: Falls back to temp directory if git.repo.path not configured (thumbnails may be lost)
- **Format Limitations**: Only supports formats that Java ImageIO/Thumbnailator support (JPG, PNG, GIF, BMP, some TIFF)
- **No Caching**: Regenerates thumbnail every time, even if file unchanged (could check modification time)
- **Synchronous Execution**: Blocks photo processing while generating thumbnail (could be async)
- **No Size Validation**: Doesn't verify source image dimensions before processing (could fail on extremely large images)
- **Fixed Quality**: JPEG quality hard-coded to 0.85 (not configurable per image or file size)
- **No Progress Tracking**: No callback or status updates during generation
- **Disk Space**: No checks for available disk space before generating
- **Temp Directory Fallback**: Using temp directory can lead to thumbnails in non-persistent storage

### Expectations

- Java ImageIO supports the image format
- Thumbnailator library (0.4.20) in classpath
- File system write access to thumbnail directory
- ConfigService provides git.repo.path configuration
- Source image file is readable and valid format
- Sufficient disk space for thumbnail file
- Photos.thumbnail_path accepts null (for failed generations)
- Thumbnails directory created automatically if missing
- Service handles concurrent requests safely (Thumbnailator is thread-safe)
- Thumbnail generation failures don't stop photo processing
- Generated thumbnails maintain aspect ratio (fit within 200x200 box)
- File size typically < 50KB for 200x200 JPEG at 0.85 quality
- Thumbnails saved as same format as original (JPG → JPG, PNG → PNG)

### Photo Processing Pipeline Integration

PhotoProcessingService.processPhoto() calls thumbnailService.generateThumbnail() which:
1. Validates photo file exists
2. Determines thumbnail directory (git.repo.path/thumbnails)
3. Creates directory if doesn't exist
4. Generates thumbnail filename (original_name_thumb.ext)
5. Uses Thumbnailator to resize to max 200x200px (aspect ratio preserved)
6. Saves thumbnail with 0.85 JPEG quality
7. Returns absolute path (or null on failure)
8. Path saved to photos.thumbnail_path column

This completes the thumbnail generation placeholder from Step 18 and enables photo table display with thumbnail previews.
