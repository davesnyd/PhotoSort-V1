# PhotoSort Application

A comprehensive photo management system that automatically discovers, indexes, and manages photos from a Git repository.

## Project Status

### Completed Steps

✅ **Step 1: Database Schema Design and Setup**
- Complete PostgreSQL database schema with 11 tables
- Hibernate/JPA entity classes for all database tables
- JPA repositories with custom query methods
- Comprehensive JUnit test suite (20+ test cases)
- Full documentation (user, developer, test plan, learnings)

✅ **Step 3: Database Connection Configuration**
- HikariCP connection pool configuration
- Advanced transaction management
- Performance optimization (batch processing, statement caching)
- Additional test suite (8 test cases)
- Updated documentation with monitoring and troubleshooting guides

✅ **Step 4: OAuth 2.0 Google Authentication**
- Google OAuth 2.0 integration
- Custom OAuth user service with PhotoSort user model integration
- User management service (create, update, admin promotion)
- Security configuration (CSRF protection, session management)
- Authentication REST API endpoints
- Comprehensive test suite (10 test cases)
- Complete user and developer documentation

✅ **Step 5: React Frontend Project**
- Modern React 18 web application
- React Router for client-side routing
- Google OAuth authentication flow
- Protected routes and admin-only routes
- Authentication state management with Context API
- Axios HTTP client with interceptors
- Responsive UI with PhotoSort color scheme
- Navigation component and login page
- Complete project structure ready for feature development

### Current Development

See `docs/WorkLog.csv` for detailed development progress.

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Database**: PostgreSQL 13+
- **ORM**: Hibernate/JPA
- **Build Tool**: Maven 3.8+
- **Testing**: JUnit 5, Spring Boot Test
- **Security**: Spring Security with OAuth 2.0
- **Utilities**: Lombok, metadata-extractor, JGit, Thumbnailator

### Frontend
- **Framework**: React 18
- **Routing**: React Router v7
- **HTTP Client**: Axios
- **State Management**: React Context API, TanStack Query (React Query)
- **Build Tool**: Create React App (react-scripts)
- **Styling**: CSS3 (component-specific stylesheets)

## Project Structure

```
PhotoSort-V1/
├── docs/                                # Documentation and tracking
│   ├── WorkLog.csv                      # Development progress log
│   ├── Learnings.md                     # Development insights
│   ├── TestPlan.md                      # Test cases
│   ├── PhotoSortUserDocumentation.md    # User guide
│   └── PhotoSortDevDocumentation.md     # Developer guide
├── PhotoSortServices/                   # Spring Boot backend
│   ├── src/main/java/com/photoSort/
│   │   ├── model/                       # JPA entities
│   │   ├── repository/                  # Data access layer
│   │   ├── service/                     # Business logic
│   │   ├── controller/                  # REST controllers
│   │   ├── config/                      # Configuration
│   │   └── exception/                   # Exception handling
│   ├── src/main/resources/
│   │   ├── schema.sql                   # Database DDL
│   │   └── application.properties       # Configuration
│   └── src/test/java/                   # Test suite
├── photosort-frontend/                  # React frontend
│   ├── public/                          # Static assets
│   ├── src/
│   │   ├── components/                  # Reusable components
│   │   ├── pages/                       # Page components
│   │   ├── services/                    # API services
│   │   ├── context/                     # React contexts
│   │   ├── utils/                       # Utility functions
│   │   ├── styles/                      # CSS files
│   │   ├── App.js                       # Root component
│   │   └── index.js                     # Entry point
│   ├── .env                             # Environment config
│   └── package.json                     # Dependencies
├── PhotoSpecification.md                # Feature specifications
└── Claude.md                            # Development instructions
```

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 13 or higher
- Maven 3.8 or higher
- Node.js 18 or higher
- npm 9 or higher

### Database Setup

1. Create PostgreSQL database:
   ```sql
   CREATE DATABASE PhotoSortData;
   CREATE DATABASE PhotoSortDataTest;  -- For testing
   ```

2. Set environment variables:
   ```bash
   export DB_USERNAME=your_postgres_username
   export DB_PASSWORD=your_postgres_password
   ```

3. Run database schema:
   ```bash
   cd PhotoSortServices/src/main/resources
   psql -U $DB_USERNAME -d PhotoSortData -f schema.sql
   ```

### Build and Run

1. Build the project:
   ```bash
   cd PhotoSortServices
   mvn clean install
   ```

2. Run tests:
   ```bash
   mvn test
   ```

3. Start the backend application:
   ```bash
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to frontend directory:
   ```bash
   cd photosort-frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure environment (create `.env` file):
   ```
   REACT_APP_API_BASE_URL=http://localhost:8080
   REACT_APP_OAUTH_REDIRECT_URI=http://localhost:3000/auth/callback
   ```

4. Start the development server:
   ```bash
   npm start
   ```

   The frontend will open in your browser at `http://localhost:3000`

5. Build for production:
   ```bash
   npm run build
   ```

### OAuth Configuration

1. Create OAuth credentials in [Google Cloud Console](https://console.cloud.google.com/)
2. Add authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google`
3. Add authorized JavaScript origins:
   - `http://localhost:8080`
   - `http://localhost:3000`
4. Set environment variables:
   ```bash
   export GOOGLE_CLIENT_ID=your_client_id
   export GOOGLE_CLIENT_SECRET=your_client_secret
   ```

## Database Schema

The application uses 11 tables organized into functional areas:

- **User Management**: users
- **Photo Storage**: photos, exif_data, photo_metadata, metadata_fields
- **Tagging**: tags, photo_tags
- **Access Control**: photo_permissions
- **Preferences**: user_column_preferences
- **Automation**: scripts, script_execution_log

See `docs/PhotoSortDevDocumentation.md` for complete schema details.

## Development Workflow

This project follows an iterative Test-Driven Development (TDD) approach:

1. **DEV**: Start development on a new step
2. **START-TESTS**: Implement initial test cases
3. **PLAN-CREATE**: Create detailed implementation plan
4. **PLAN-IMPLEMENT**: Implement the plan
5. **TESTS-AUGMENTED**: Add additional test cases
6. **CURRENT-TESTS**: Verify current tests pass
7. **ALL-TESTS**: Run full regression test suite
8. **DOCS**: Update all documentation

See `Claude.md` for complete development process.

## Documentation

- **User Documentation**: `docs/PhotoSortUserDocumentation.md`
- **Developer Documentation**: `docs/PhotoSortDevDocumentation.md`
- **Test Plan**: `docs/TestPlan.md`
- **Learnings**: `docs/Learnings.md`
- **Work Log**: `docs/WorkLog.csv`

## Testing

The project includes comprehensive automated tests:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DatabaseSchemaTest

# Run with coverage
mvn test jacoco:report
```

## License

Copyright 2025, David Snyderman

## Next Steps

See `PhotoSpecification.md` for upcoming features:
- Step 6: Admin Navigation and User Table Page
- Step 7: Photo Table Page
- Step 8: Modify Columns Dialog
- Step 9: User Access Dialog
- Step 10: Image Display Page
- And more...
