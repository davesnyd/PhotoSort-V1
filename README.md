# PhotoSort Application

A comprehensive photo management system that automatically discovers, indexes, and manages photos from a Git repository. Built with Spring Boot (Java 17), React 18, and PostgreSQL.

## Quick Links

📚 **Getting Started**
- [Installation Guide](#getting-started) - Set up PhotoSort locally
- [Docker Deployment](DOCKER.md) - Deploy with Docker Compose
- [AWS Deployment](DOCKER_IMAGES.md#aws-deployment) - Deploy to AWS (EC2, ECS, Copilot)

🔧 **Operations**
- [Build & Deploy Guide](BUILD_AND_DEPLOY.md) - Build, configure, and deploy
- [Troubleshooting Guide](TROUBLESHOOTING.md) - Fix common issues
- [Maintenance Guide](MAINTENANCE.md) - Keep PhotoSort running smoothly

🔐 **Security**
- [Secrets Setup](SECRETS_SETUP.md) - Configure OAuth, database, Git credentials
- [Credential Restoration](CREDENTIAL_RESTORATION_GUIDE.md) - Recover from credential exposure

📖 **Documentation**
- [User Documentation](docs/PhotoSortUserDocumentation.md) - How to use PhotoSort
- [Developer Documentation](docs/PhotoSortDevDocumentation.md) - Architecture and implementation
- [API Documentation](#api-documentation) - REST API reference

## Features

✅ **Completed Features** (Steps 1-20)

**User Management**
- Google OAuth 2.0 authentication
- Role-based access control (users and administrators)
- User profile management
- Admin user management table

**Photo Management**
- Photo table with search, sort, and pagination
- Permission-based photo access control
- Public/private photo visibility
- Photo metadata and EXIF data extraction
- Tag management and assignment
- Custom column preferences per user

**Advanced Features**
- Advanced filtering (must contain/must not contain)
- Image display with metadata editing
- User access permissions dialog
- Script management and execution
- Git repository polling for photo updates
- Automated script execution

**Technical Features**
- Comprehensive automated test coverage (backend and frontend)
- Docker containerization
- Production-ready deployment configurations
- Complete documentation and maintenance guides

### Current Development

See `docs/WorkLog.csv` for detailed development progress and `PhotoSpecification.md` for planned features.

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

## API Documentation

PhotoSort exposes a RESTful API for all operations. The backend runs on port 8080 with the following base URL:
```
http://localhost:8080
```

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/user` | Get current authenticated user |
| POST | `/api/auth/logout` | Logout current user |

### User Management Endpoints (Admin Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users (paginated, sortable) |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update user (display name, admin status) |

### Photo Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/photos` | Get photos (permission-filtered, paginated, sortable) |
| GET | `/api/photos/{id}` | Get photo by ID |
| PUT | `/api/photos/{id}` | Update photo (tags, metadata, visibility) |
| PUT | `/api/photos/{id}/visibility` | Update photo visibility (public/private) |

**Query Parameters**:
- `page`: Page number (0-indexed)
- `size`: Page size (default: 10)
- `sortBy`: Field to sort by (fileName, createdDate, ownerDisplayName, etc.)
- `sortDir`: Sort direction (asc/desc)
- `search`: Quick search term (searches fileName and filePath)
- `filterField1`, `filterValue1`, `filterType1`: Advanced filter 1
- `filterField2`, `filterValue2`, `filterType2`: Advanced filter 2

### Photo Permission Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/photos/{id}/permissions` | Get all permissions for a photo |
| POST | `/api/photos/{id}/permissions` | Grant permission to a user |
| DELETE | `/api/photos/{photoId}/permissions/{userId}` | Revoke permission |

### Script Endpoints (Admin Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/scripts` | Get all scripts (paginated, sortable) |
| GET | `/api/scripts/{id}` | Get script by ID |
| POST | `/api/scripts` | Create new script |
| PUT | `/api/scripts/{id}` | Update script |
| DELETE | `/api/scripts/{id}` | Delete script |
| POST | `/api/scripts/{id}/execute` | Execute script |

### Tag Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tags` | Get all tags |
| GET | `/api/tags/{id}` | Get tag by ID |
| POST | `/api/tags` | Create new tag |
| DELETE | `/api/tags/{id}` | Delete tag |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health status |

## Documentation Index

### User Guides
- **[User Documentation](docs/PhotoSortUserDocumentation.md)** - How to use PhotoSort features
- **[Test Plan](docs/TestPlan.md)** - Manual testing procedures

### Deployment Guides
- **[Getting Started](#getting-started)** - Local development setup (below)
- **[Docker Deployment](DOCKER.md)** - Docker Compose deployment (400+ lines)
- **[Docker Images Reference](DOCKER_IMAGES.md)** - Technical details, AWS deployment (1000+ lines)
- **[Build & Deploy Guide](BUILD_AND_DEPLOY.md)** - Non-Docker deployment options

### Operations Guides
- **[Troubleshooting](TROUBLESHOOTING.md)** - Common issues and solutions (500+ lines)
- **[Maintenance Guide](MAINTENANCE.md)** - Regular maintenance tasks, backups, monitoring (600+ lines)
- **[Secrets Setup](SECRETS_SETUP.md)** - OAuth, database, Git credential management
- **[Credential Restoration](CREDENTIAL_RESTORATION_GUIDE.md)** - Recovery from credential exposure

### Development Guides
- **[Developer Documentation](docs/PhotoSortDevDocumentation.md)** - Architecture, implementation details
- **[Testing Documentation](TESTING.md)** - Automated testing approach
- **[Frontend Testing Plan](docs/FrontendTestingPlan.md)** - Frontend test strategy
- **[Work Log](docs/WorkLog.csv)** - Development progress tracking
- **[Learnings](docs/Learnings.md)** - Development insights and improvements
- **[CLAUDE.md](CLAUDE.md)** - Instructions for Claude Code development workflow

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
