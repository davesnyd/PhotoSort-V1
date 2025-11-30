# PhotoSort Application Specification

## Document Version
Version 1.1 - 2025-11-30

## Goals and Objectives
PhotoSort is a web-based photo management system that:
- Automatically discovers and indexes photos from a Git repository
- Extracts and stores EXIF metadata and custom tags
- Provides search and viewing capabilities for users
- Allows administrators to manage users, photos, and automated scripts
- Supports user-level privacy controls for photos

## Non-Functional Requirements

### Performance
- Search results must return within 2 seconds for datasets up to 10,000 photos
- Photo thumbnails must load within 1 second
- Page navigation must be responsive (< 300ms)

### Scalability
- Support up to 100 concurrent users
- Handle photo libraries up to 100,000 images
- Database must be optimized with proper indexing

### Security
- All passwords and secrets stored using encryption or environment variables (never plaintext)
- HTTPS required for all communications
- OAuth 2.0 for authentication
- SQL injection prevention via parameterized queries (Hibernate)
- XSS prevention in React components
- CSRF protection on all POST/PUT/DELETE endpoints
- Input validation on all user inputs
- File upload validation (type, size, content)

### Browser Support
- Chrome (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Edge (latest 2 versions)

### Mobile Responsiveness
- Responsive design for tablets (768px and above)
- Mobile support is nice-to-have but not required in initial version

## Technology Stack

### Components
1. **Database**: PostgreSQL 13+
2. **Backend**: Java 17+ with Spring Boot 3.x
   - Spring Web (REST APIs)
   - Spring Data JPA (Hibernate for ORM)
   - Spring Security with OAuth 2.0 (Google authentication)
   - JGit (Git repository interaction)
   - metadata-extractor library (EXIF data extraction)
3. **Frontend**: React 18+ with JavaScript
   - React Router (navigation)
   - Axios (HTTP client)
   - State management: React Context API or Redux (TBD)
4. **Scripts**: Python 3.8+ or Bash
5. **Configuration**: JSON format
6. **STAG Package**: Python-based photo tagging system (stag-main/stag.py)
7. **Git Repository**: Contains source images and metadata files

### Configuration File Format
Configuration stored in `config.json` (JSON format). Example:
```json
{
  "database": {
    "uri": "jdbc:postgresql://localhost:5432/PhotoSortData",
    "username": "${DB_USERNAME}",
    "password": "${DB_PASSWORD}"
  },
  "git": {
    "repoPath": "/path/to/photo/repository",
    "url": "https://github.com/username/photos.git",
    "username": "${GIT_USERNAME}",
    "token": "${GIT_TOKEN}",
    "pollIntervalMinutes": 5
  },
  "oauth": {
    "clientId": "${OAUTH_CLIENT_ID}",
    "clientSecret": "${OAUTH_CLIENT_SECRET}",
    "redirectUri": "http://localhost:8080/oauth2/callback"
  },
  "stag": {
    "scriptPath": "./stag-main/stag.py",
    "pythonExecutable": "python3"
  }
}
```

**Note**: Values with `${VARIABLE}` syntax are read from environment variables for security.

## Background

### Application Overview
The front end of the application is in React. The backend uses Spring Boot connected to a PostgreSQL database.

### Color Scheme
- **Primary**: Burgundy (#800020)
- **Secondary**: Navy Blue (#000080)
- **Accent**: Cream (#FFFDD0)

### Workflow
1. Git repository contains photos and optional metadata files
2. Backend polls Git repository periodically for new/changed files
3. When new image detected:
   - STAG Python script runs to generate AI tags
   - EXIF data extracted using metadata-extractor library
   - Optional `.metadata` file parsed (same filename as image, different extension)
   - All data stored in PostgreSQL database
4. Users access web interface to search, view, and manage photos

### Permission Levels
- **User**: Can search, view, and edit metadata for their own photos and public photos
- **Administrator**: Can manage all photos, all users, and system scripts

### Metadata File Format
Optional `.metadata` files contain custom metadata in key-value format:
```
Title=Family Vacation 2024
Location=Grand Canyon, Arizona
tags=vacation,family,nature,landscape
Event=Summer Trip
People=John,Jane,Kids
```

- Each line: `fieldname=value`
- Special field `tags`: comma-separated list of tags
- Other fields become custom metadata entries

### EXIF Fields to Extract
Common EXIF fields to store:
- Date/Time Original
- Camera Make
- Camera Model
- GPS Latitude
- GPS Longitude
- Exposure Time
- F-Number
- ISO Speed
- Focal Length
- Image Width
- Image Height
- Orientation

## UI Design System

### Standard Table Component
All table pages follow this design pattern:

#### Table Header
- Background: Navy blue (#000080)
- Text: Cream (#FFFDD0)
- Font weight: Bold
- Clicking column header sorts ascending
- Clicking again sorts descending
- Active sort column shows indicator (▲ or ▼)

#### Table Rows
- Alternating row colors:
  - Even rows: Cream background (#FFFDD0), Navy text (#000080)
  - Odd rows: Burgundy background (#800020), Cream text (#FFFDD0)
- Hover state: Slight opacity change (0.9)

#### Pagination Controls
Located above and below table:
- **First Page**: ⏮ (double left arrow)
- **Previous Page**: ◀ (single left arrow)
- **Rows Per Page**: Dropdown {10, 20, 50}, default 10
- **Next Page**: ▶ (single right arrow)
- **Last Page**: ⏭ (double right arrow)
- Current page indicator: "Page X of Y"

#### Search Controls
Located above top pagination:

**Quick Search** (default):
- Single text input field (full width)
- "Search" button
- Searches across all visible columns
- Case-insensitive partial match

**Advanced Search**:
- Radio button to toggle between Quick/Advanced
- Three filter rows, each with:
  - Column dropdown (all table columns)
  - Text input for value
  - "Must contain" / "Must not contain" dropdown
- "Search" button
- All filters combined with AND logic

## REST API Specification

### Authentication Endpoints
- `POST /api/auth/google` - Initiate Google OAuth flow
- `GET /api/auth/callback` - OAuth callback handler
- `POST /api/auth/logout` - Logout current user
- `GET /api/auth/current` - Get current authenticated user

### User Management (Admin only)
- `GET /api/users` - List all users (paginated, searchable, sortable)
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user (change user type)
- `GET /api/users/{id}/photos` - Get photos owned by user

### Photo Management
- `GET /api/photos` - List photos (filtered by permissions, paginated, searchable, sortable)
- `GET /api/photos/{id}` - Get photo details
- `GET /api/photos/{id}/image` - Get photo file (binary)
- `GET /api/photos/{id}/thumbnail` - Get thumbnail (binary)
- `PUT /api/photos/{id}` - Update photo metadata
- `PUT /api/photos/{id}/tags` - Update photo tags
- `PUT /api/photos/{id}/permissions` - Update photo user permissions
- `GET /api/photos/{id}/permissions` - Get photo user permissions

### Metadata Management
- `GET /api/metadata/fields` - List all metadata field names
- `GET /api/users/{userId}/columns` - Get user's selected columns
- `PUT /api/users/{userId}/columns` - Update user's selected columns

### Script Management (Admin only)
- `GET /api/scripts` - List all scripts
- `GET /api/scripts/{id}` - Get script details
- `POST /api/scripts` - Create new script
- `PUT /api/scripts/{id}` - Update script
- `DELETE /api/scripts/{id}` - Delete script

### Configuration Management (Admin only)
- `GET /api/config` - Get configuration (passwords redacted)
- `PUT /api/config` - Update configuration

### Common Response Format
**Success:**
```json
{
  "success": true,
  "data": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message"
  }
}
```

### Pagination Format
```json
{
  "content": [ ... ],
  "page": 0,
  "pageSize": 10,
  "totalPages": 25,
  "totalElements": 243
}
```

## Implementation Steps

### Step 1: Database Schema Design and Setup
**Components**: PostgreSQL database, Hibernate entities

**Description**: Create a PostgreSQL database named `PhotoSortData` with the following tables:

#### Database Tables

**users**
- `user_id` (BIGSERIAL, PRIMARY KEY)
- `google_id` (VARCHAR(255), UNIQUE, NOT NULL) - Google OAuth user ID
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `display_name` (VARCHAR(255))
- `user_type` (VARCHAR(20), NOT NULL) - 'USER' or 'ADMIN'
- `first_login_date` (TIMESTAMP, NOT NULL)
- `last_login_date` (TIMESTAMP, NOT NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- INDEX on `google_id`, `email`

**photos**
- `photo_id` (BIGSERIAL, PRIMARY KEY)
- `owner_id` (BIGINT, FOREIGN KEY → users.user_id)
- `file_name` (VARCHAR(500), NOT NULL)
- `file_path` (VARCHAR(1000), NOT NULL) - Relative path in Git repo
- `file_size` (BIGINT) - Size in bytes
- `file_created_date` (TIMESTAMP)
- `file_modified_date` (TIMESTAMP)
- `added_to_system_date` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- `is_public` (BOOLEAN, DEFAULT FALSE)
- `image_width` (INTEGER)
- `image_height` (INTEGER)
- `thumbnail_path` (VARCHAR(1000)) - Path to generated thumbnail
- INDEX on `owner_id`, `file_name`, `added_to_system_date`
- UNIQUE constraint on `file_path`

**exif_data**
- `exif_id` (BIGSERIAL, PRIMARY KEY)
- `photo_id` (BIGINT, FOREIGN KEY → photos.photo_id, ON DELETE CASCADE)
- `date_time_original` (TIMESTAMP)
- `camera_make` (VARCHAR(100))
- `camera_model` (VARCHAR(100))
- `gps_latitude` (DECIMAL(10, 8))
- `gps_longitude` (DECIMAL(11, 8))
- `exposure_time` (VARCHAR(50))
- `f_number` (VARCHAR(50))
- `iso_speed` (INTEGER)
- `focal_length` (VARCHAR(50))
- `orientation` (INTEGER)
- INDEX on `photo_id`

**metadata_fields**
- `field_id` (BIGSERIAL, PRIMARY KEY)
- `field_name` (VARCHAR(100), UNIQUE, NOT NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- INDEX on `field_name`

**photo_metadata**
- `metadata_id` (BIGSERIAL, PRIMARY KEY)
- `photo_id` (BIGINT, FOREIGN KEY → photos.photo_id, ON DELETE CASCADE)
- `field_id` (BIGINT, FOREIGN KEY → metadata_fields.field_id)
- `metadata_value` (TEXT)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- INDEX on `photo_id`, `field_id`
- UNIQUE constraint on (`photo_id`, `field_id`)

**tags**
- `tag_id` (BIGSERIAL, PRIMARY KEY)
- `tag_value` (VARCHAR(100), UNIQUE, NOT NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- INDEX on `tag_value`

**photo_tags**
- `photo_tag_id` (BIGSERIAL, PRIMARY KEY)
- `photo_id` (BIGINT, FOREIGN KEY → photos.photo_id, ON DELETE CASCADE)
- `tag_id` (BIGINT, FOREIGN KEY → tags.tag_id, ON DELETE CASCADE)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- INDEX on `photo_id`, `tag_id`
- UNIQUE constraint on (`photo_id`, `tag_id`)

**photo_permissions**
- `permission_id` (BIGSERIAL, PRIMARY KEY)
- `photo_id` (BIGINT, FOREIGN KEY → photos.photo_id, ON DELETE CASCADE)
- `user_id` (BIGINT, FOREIGN KEY → users.user_id, ON DELETE CASCADE)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- INDEX on `photo_id`, `user_id`
- UNIQUE constraint on (`photo_id`, `user_id`)

**user_column_preferences**
- `preference_id` (BIGSERIAL, PRIMARY KEY)
- `user_id` (BIGINT, FOREIGN KEY → users.user_id, ON DELETE CASCADE)
- `column_type` (VARCHAR(50), NOT NULL) - 'STANDARD' or 'METADATA'
- `column_name` (VARCHAR(100)) - For STANDARD: file_name, thumbnail, etc. For METADATA: field name
- `display_order` (INTEGER, NOT NULL)
- INDEX on `user_id`
- UNIQUE constraint on (`user_id`, `column_type`, `column_name`)

**scripts**
- `script_id` (BIGSERIAL, PRIMARY KEY)
- `script_name` (VARCHAR(100), UNIQUE, NOT NULL)
- `script_file_name` (VARCHAR(255))
- `script_contents` (TEXT)
- `run_time` (TIME) - Daily run time (nullable)
- `periodicity_minutes` (INTEGER) - Periodic run interval (nullable)
- `file_extension` (VARCHAR(20)) - File type this script processes
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- INDEX on `file_extension`

**script_execution_log**
- `log_id` (BIGSERIAL, PRIMARY KEY)
- `script_id` (BIGINT, FOREIGN KEY → scripts.script_id)
- `photo_id` (BIGINT, FOREIGN KEY → photos.photo_id)
- `execution_time` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- `status` (VARCHAR(20)) - 'SUCCESS', 'FAILURE'
- `error_message` (TEXT)
- INDEX on `script_id`, `execution_time`

**Test Cases**:
1. Connect to database using credentials from environment variables
2. Create all tables with proper schema
3. Verify all foreign key constraints exist
4. Verify all indexes are created
5. Insert sample user record and verify
6. Insert sample photo record with foreign key to user and verify
7. Insert EXIF data for photo and verify cascade
8. Query photos with JOIN to users and verify relationship
9. Test deletion cascade (delete user, verify photos deleted)
10. Verify unique constraints (attempt duplicate insert)

---

### Step 2: Spring Boot Project Setup
**Components**: Maven, Spring Boot, project structure

**Description**: Create a Spring Boot Maven project named `PhotoSortServices` with the following structure:

```
PhotoSortServices/
├── src/main/java/com/photoSort/
│   ├── PhotoSortApplication.java
│   ├── config/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   ├── dto/
│   └── exception/
├── src/main/resources/
│   ├── application.properties
│   └── application-dev.properties
├── src/test/java/
└── pom.xml
```

**Maven Dependencies**:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-oauth2-client
- postgresql
- lombok
- metadata-extractor
- org.eclipse.jgit
- spring-boot-starter-test

**Test Cases**:
1. Verify Spring Boot application starts successfully
2. Verify all dependencies are resolved
3. Verify application.properties loads correctly
4. Verify dev profile can be activated
5. Run `mvn clean install` and confirm success

---

### Step 3: Database Connection Configuration
**Components**: Spring Data JPA, Hibernate, PostgreSQL

**Description**: Configure Spring Boot to connect to PostgreSQL database. Create Hibernate entity classes for all database tables. Configure connection pooling and transaction management.

**Configuration** (application.properties):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/PhotoSortData
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.hikari.maximum-pool-size=10
```

**Entity Classes to Create**:
- User.java
- Photo.java
- ExifData.java
- MetadataField.java
- PhotoMetadata.java
- Tag.java
- PhotoTag.java
- PhotoPermission.java
- UserColumnPreference.java
- Script.java
- ScriptExecutionLog.java

**Test Cases**:
1. Verify database connection successful on startup
2. Verify all entity classes map correctly to database tables
3. Create JPA repositories for each entity
4. Test CRUD operations on User entity
5. Test CRUD operations on Photo entity with foreign key to User
6. Test cascade operations (delete user, verify related data deleted)
7. Verify Hibernate generates correct SQL queries
8. Test transaction rollback on error

---

### Step 4: OAuth 2.0 Google Authentication
**Components**: Spring Security, OAuth 2.0 client

**Description**: Configure Google OAuth 2.0 authentication. Implement security configuration to protect endpoints. Create user service to handle first-time login (create user record) and returning users (update last login).

**Test Cases**:
1. Verify OAuth configuration loads from environment variables
2. Initiate OAuth flow and redirect to Google
3. Handle OAuth callback successfully
4. Create new user record on first login
5. Update existing user's last login date on subsequent logins
6. Verify JWT token or session created after successful auth
7. Verify unauthenticated requests to protected endpoints return 401
8. Verify authenticated requests include user context
9. Test logout functionality
10. Verify CSRF protection enabled

---

### Step 5: Create React Frontend Project
**Components**: React, React Router, Axios

**Description**: Create a React application named `PhotoSort` with routing and API client configuration.

**Project Structure**:
```
photoSort-frontend/
├── public/
├── src/
│   ├── components/
│   ├── pages/
│   ├── services/
│   ├── context/
│   ├── utils/
│   ├── App.js
│   └── index.js
├── package.json
└── .env
```

**Dependencies**:
- react
- react-router-dom
- axios
- react-query (for API state management)

**Test Cases**:
1. Verify React app starts successfully (npm start)
2. Verify React Router configured correctly
3. Create Axios instance with base URL to backend
4. Test API client can make authenticated requests
5. Create login page with "Login with Google" button
6. Verify OAuth flow redirects to Google
7. Handle OAuth callback and store auth token
8. Verify protected routes redirect to login if not authenticated
9. Create navigation component
10. Verify logout clears authentication state

---

### Step 6: Admin Navigation and User Table Page
**Components**: Frontend, backend, database

**Description**: Create admin navigation menu and user management table page.

**Admin Navigation Bar** (visible only to administrators):
- Menu items: "Users", "Photos", "Scripts", "Configuration"
- Clicking "Users" → User Table Page
- Clicking "Photos" → Photo Table Page (all photos)
- Clicking "Scripts" → Scripts Table Page
- Clicking "Configuration" → Edit Configuration Page
- Color scheme: Navy blue background, cream text

**User Table Columns**:
- Display Name
- Email
- First Login Date (formatted)
- Last Login Date (formatted)
- User Type (dropdown: User/Administrator - editable)
- Number of Images Owned (calculated)
- Actions column: "View Images" button

**Backend API Required**:
- `GET /api/users?page=X&size=Y&sort=field&search=term` - Get paginated users with photo count
- `PUT /api/users/{id}` - Update user type

**Test Cases**:
1. Verify only administrators can access user table page
2. Verify all users displayed with correct data
3. Verify photo count calculated correctly for each user
4. Verify pagination controls work (first, prev, next, last)
5. Verify rows per page dropdown works (10, 20, 50)
6. Verify quick search filters users correctly
7. Verify advanced search works with multiple criteria
8. Verify column sorting works (ascending/descending)
9. Verify admin can change user type from User to Administrator
10. Verify "View Images" button navigates to photo table filtered by that user
11. Verify non-admin users get 403 error when accessing this page

---

### Step 7: Photo Table Page
**Components**: Frontend, backend, database

**Description**: Create photo table page with different views for users vs administrators.

**User View** (non-admin):
- Shows photos the user can access:
  - Photos owned by the user
  - Public photos
  - Private photos where user has been granted permission
- Default columns: File Name, File Creation Date, Thumbnail
- "Modify Columns" button in top toolbar

**Admin View**:
- Shows all photos in system (or filtered by specific user if coming from User Table)
- Columns: File Name, Owner, File Creation Date, File Update Date, Thumbnail, Actions
- Actions column contains:
  - "View Image" button (always visible)
  - "Set Users" button (only for private photos)

**Backend API Required**:
- `GET /api/photos?page=X&size=Y&sort=field&search=term&userId=Z` - Get paginated photos with permission filtering
- `GET /api/photos/{id}/thumbnail` - Get thumbnail image

**Test Cases**:
1. Verify user sees only authorized photos (owned, public, or granted access)
2. Verify admin sees all photos (or filtered by user if parameter provided)
3. Verify user's custom columns from preferences are displayed
4. Verify default columns shown for new users
5. Verify thumbnails display correctly
6. Verify "View Image" button navigates to Image Display Page
7. Verify "Set Users" button only appears for private photos
8. Verify "Set Users" button opens User Access Dialog
9. Verify "Modify Columns" button opens Modify Columns Dialog
10. Verify quick search works across visible columns
11. Verify advanced search allows selection of columns with must/must not contain
12. Verify advanced search filters correctly with AND logic
13. Verify column sorting works (ascending/descending)
14. Verify pagination controls work correctly
15. Verify public photos visible to all users

---

### Step 8: Modify Columns Dialog
**Components**: Frontend, backend, database

**Description**: Create popup dialog for users to customize which columns appear in their photo table.

**Dialog Layout**:
- **Title**: "Customize Photo Table Columns"
- **Column List** (scrollable):
  - Standard columns: File Name, File Creation Date, File Modified Date, Owner, Tags, Thumbnail
  - All EXIF fields: Camera Make, Camera Model, Date/Time Original, GPS Location, etc.
  - All custom metadata fields (from metadata_fields table)
  - Each item has checkbox on left
  - Checked = column visible, unchecked = column hidden
- **Buttons**:
  - "Save" - Save preferences
  - "Cancel" - Close without saving

**Backend API Required**:
- `GET /api/metadata/fields` - Get all available metadata field names
- `GET /api/users/{userId}/columns` - Get user's current column preferences
- `PUT /api/users/{userId}/columns` - Save column preferences

**Test Cases**:
1. Verify dialog displays all available columns (standard + EXIF + custom metadata)
2. Verify currently displayed columns have checkboxes checked
3. Verify hidden columns have checkboxes unchecked
4. Verify user can check/uncheck columns
5. Verify "Save" button updates user_column_preferences table
6. Verify photo table refreshes with new columns after save
7. Verify "Cancel" button closes dialog without saving changes
8. Verify at least one column must remain checked (validation)
9. Verify new metadata fields appear in list when added to system

---

### Step 9: User Access Dialog
**Components**: Frontend, backend, database

**Description**: Create popup dialog for managing which users can access a private photo.

**Dialog Layout**:
- **Title**: "Manage Photo Access - [Photo Filename]"
- **User List** (scrollable):
  - One row per user in system (excluding photo owner)
  - Each row: Checkbox | Display Name | Email
  - Checked = user has access
  - Unchecked = user doesn't have access
- **Buttons**:
  - "Save" - Save permissions
  - "Cancel" - Close without saving

**Backend API Required**:
- `GET /api/photos/{photoId}/permissions` - Get current permissions
- `PUT /api/photos/{photoId}/permissions` - Update permissions (array of user IDs)

**Test Cases**:
1. Verify dialog displays all users except photo owner
2. Verify users with current access have checkboxes checked
3. Verify users without access have checkboxes unchecked
4. Verify user can check/uncheck permissions
5. Verify "Save" button updates photo_permissions table (removes old, adds new)
6. Verify "Cancel" button closes without saving
7. Verify only photo owner and admins can access this dialog
8. Verify changes take effect immediately (user gains/loses access)

---

### Step 10: Image Display Page
**Components**: Frontend, backend, database

**Description**: Create detailed photo view page with image display and metadata editing.

**Page Layout**:
- **Left Side** (70% width): Large photo display
  - Full-resolution image
  - Zoom/pan controls
- **Right Side** (30% width): Metadata panel
  - **EXIF Data** (read-only section):
    - Camera Make, Model
    - Date/Time Original
    - GPS Location (if available)
    - Exposure, ISO, Focal Length, etc.
  - **Custom Metadata** (editable section):
    - List of field name: value pairs
    - Each field has edit/delete icons
    - "Add Field" button at bottom
  - **Tags** (editable section):
    - Tag chips/pills
    - Each tag has delete icon
    - "Add Tag" text input
- **Bottom**: "Return to List" button

**Editing Functionality**:
- Click edit icon on metadata field → inline edit
- Click delete icon → confirm and remove
- Click "Add Field" → modal with field name and value inputs
- Type in "Add Tag" input and press Enter → add tag

**Backend API Required**:
- `GET /api/photos/{id}` - Get full photo details with all metadata
- `GET /api/photos/{id}/image` - Get full-resolution image
- `PUT /api/photos/{id}` - Update photo metadata
- `PUT /api/photos/{id}/tags` - Update tags

**Test Cases**:
1. Verify correct photo image is displayed
2. Verify all EXIF data displayed correctly in read-only section
3. Verify all custom metadata displayed with edit/delete options
4. Verify all tags displayed as removable chips
5. Verify user can edit custom metadata field value
6. Verify user can delete custom metadata field
7. Verify user can add new custom metadata field
8. Verify user can add new tag
9. Verify user can delete tag
10. Verify changes persist to database
11. Verify "Return to List" navigates back to Photo Table with previous state (page, sort, search)
12. Verify only authorized users can view photo (owner, admins, or granted permission)
13. Verify GPS coordinates displayed as map link if available

---

### Step 11: Scripts Table Page (Admin Only)
**Components**: Frontend, backend, database

**Description**: Create admin page for managing automated scripts.

**Page Layout**:
- Standard table with columns:
  - Script Name
  - Script File Name
  - Run Time (daily schedule)
  - Periodicity (recurring interval)
  - File Extension (file type to process)
  - Actions: "Edit" button
- "Add Script" button above table

**Backend API Required**:
- `GET /api/scripts` - Get all scripts (paginated, searchable, sortable)
- `GET /api/scripts/{id}` - Get script details
- `POST /api/scripts` - Create new script
- `PUT /api/scripts/{id}` - Update script
- `DELETE /api/scripts/{id}` - Delete script

**Test Cases**:
1. Verify only admins can access scripts page
2. Verify all scripts displayed from database
3. Verify script information displayed correctly
4. Verify "Add Script" button opens Edit Script Dialog with blank fields
5. Verify "Edit" button opens Edit Script Dialog with current values
6. Verify pagination/search/sort work correctly
7. Verify non-admin users get 403 error

---

### Step 12: Edit Script Dialog (Admin Only)
**Components**: Frontend, backend, database

**Description**: Create dialog for creating/editing automated scripts.

**Dialog Layout**:
- **Script Name**: Text input
- **Script File Name**: File selector (browse for Python/Bash script)
- **Run Time**: Time picker (HH:MM, 24-hour format) - for daily execution
- **Periodicity**: Dropdown
  - Options: None, 1 minute, 5 minutes, 10 minutes, 1 hour, 2 hours, 6 hours, 1 day
  - Note: Run Time and Periodicity are mutually exclusive
- **File Extension**: Text input (e.g., ".jpg", ".png")
- **Script Contents**: Large text area (code editor)
- **Buttons**: "Save", "Delete" (if editing), "Cancel"

**Backend API Required**:
- Same as Step 11

**Script Engine Notification**:
- When "Save" clicked, backend triggers script engine to reload configuration

**Test Cases**:
1. Verify dialog opens with correct values (empty for new, populated for edit)
2. Verify user can input/edit all fields
3. Verify Run Time and Periodicity validation (only one can be set)
4. Verify "Save" button persists changes to database
5. Verify "Save" triggers script engine reload
6. Verify "Delete" button removes script after confirmation
7. Verify "Cancel" button closes without saving
8. Verify script table refreshed after save/delete with previous state maintained
9. Verify file upload for script file works
10. Verify script contents text area supports code editing (syntax highlighting nice-to-have)

---

### Step 13: Configuration Management Page (Admin Only)
**Components**: Frontend, backend, configuration file

**Description**: Create admin page for editing system configuration.

**Page Layout**:
Form with labeled inputs for:
- **Database Configuration**:
  - Database URI (text input)
  - Database Username (text input)
  - Database Password (password input - shown as dots)
- **Git Configuration**:
  - Repository Path (directory selector)
  - Git URL (text input)
  - Git Username (text input)
  - Git Access Token (password input)
  - Poll Interval (number input with minutes unit)
- **OAuth Configuration**:
  - Client ID (text input)
  - Client Secret (password input)
  - Redirect URI (text input)
- **STAG Configuration**:
  - Script Path (file selector)
  - Python Executable Path (text input)
- **"Save Configuration" button** at bottom

**Backend API Required**:
- `GET /api/config` - Get configuration (passwords redacted to "********")
- `PUT /api/config` - Update configuration
  - Only updates fields where value != "********"
  - Validates configuration before saving
  - Updates config.json file with environment variable references intact

**Security Note**: Passwords displayed as "********". Only update if user changes value.

**Test Cases**:
1. Verify only admins can access configuration page
2. Verify configuration loads correctly with passwords redacted
3. Verify user can update database URI
4. Verify user can update Git settings
5. Verify unchanged password fields remain as environment variables
6. Verify changed password fields update with new values
7. Verify configuration saved to config.json correctly
8. Verify invalid configuration rejected (e.g., invalid URI format)
9. Verify system uses new configuration after save
10. Verify non-admin users get 403 error

---

### Step 14: Git Repository Polling Service
**Components**: Backend, JGit, Git repository

**Description**: Create background service to poll Git repository for new/changed photo files.

**Service Responsibilities**:
1. Read Git configuration from config.json
2. On startup and at configured interval:
   - Execute `git pull` on configured repository
   - Use JGit to detect changed files since last poll
   - Filter for image files (jpg, jpeg, png, gif, etc.)
   - Process each new/changed image file

**Backend Implementation**:
- Spring @Scheduled task or separate thread
- Use JGit DiffFormatter to find changed files
- Track last processed commit hash

**Test Cases**:
1. Verify service starts on application startup
2. Verify Git pull executes at configured interval
3. Verify JGit correctly detects new files
4. Verify JGit correctly detects modified files
5. Verify only image files are processed
6. Verify last commit hash tracked correctly
7. Verify service handles Git authentication errors gracefully
8. Verify service handles repository not found error
9. Verify service logs all operations
10. Verify interval can be updated via configuration

---

### Step 15: EXIF Data Extraction
**Components**: Backend, metadata-extractor library

**Description**: Create service to extract EXIF data from photos using metadata-extractor library.

**Service Responsibilities**:
1. Accept photo file path as input
2. Extract EXIF data using metadata-extractor
3. Parse relevant fields (see EXIF fields in Background section)
4. Return structured ExifData object
5. Handle photos without EXIF data gracefully

**Backend Implementation**:
- Create ExifDataService with extractExifData(File photo) method
- Map metadata-extractor output to ExifData entity
- Handle GPS coordinates conversion to decimal format

**Test Cases**:
1. Verify EXIF extraction from JPG with full EXIF data
2. Verify EXIF extraction from PNG (limited EXIF)
3. Verify handling of image without EXIF data (return null/empty)
4. Verify GPS coordinates extracted and converted correctly
5. Verify date/time formats parsed correctly
6. Verify camera make/model extracted correctly
7. Verify exposure settings extracted correctly
8. Verify image dimensions extracted correctly
9. Verify orientation value extracted correctly
10. Verify corrupted EXIF data handled gracefully (no crash)

---

### Step 16: Metadata File Parsing
**Components**: Backend, file I/O

**Description**: Create service to parse optional .metadata files accompanying photos.

**Service Responsibilities**:
1. Check if .metadata file exists for given photo file
2. Parse key=value format (see Metadata File Format in Background)
3. Handle special "tags" field (comma-separated)
4. Create/retrieve metadata fields in metadata_fields table
5. Return map of field names to values

**Backend Implementation**:
- Create MetadataParserService with parseMetadataFile(File metadataFile) method
- Parse each line as key=value
- Handle tags specially: split on comma, trim whitespace
- Create new metadata field entries as needed

**Test Cases**:
1. Verify parsing of well-formatted .metadata file
2. Verify tags field parsed as comma-separated list
3. Verify new metadata field names created in metadata_fields table
4. Verify existing metadata field names reused
5. Verify handling of missing .metadata file (return empty map)
6. Verify handling of malformed lines (skip or log warning)
7. Verify handling of duplicate keys (last value wins)
8. Verify empty values handled correctly
9. Verify special characters in values handled correctly
10. Verify Unicode/international characters supported

---

### Step 17: STAG Script Integration
**Components**: Backend, STAG Python script, process execution

**Description**: Create service to execute STAG Python script for AI-generated photo tagging.

**Service Responsibilities**:
1. Accept photo file path as input
2. Execute STAG Python script with photo as argument
3. Capture output (list of generated tags)
4. Parse output and return as list of tag strings
5. Handle script execution errors

**Backend Implementation**:
- Create StagService with generateTags(File photo) method
- Use ProcessBuilder to execute Python script
- Parse script output (assume comma-separated tags or one per line)
- Configure timeout to prevent hanging

**Test Cases**:
1. Verify STAG script executes successfully
2. Verify tags returned correctly
3. Verify multiple tags parsed from output
4. Verify script timeout after configured duration
5. Verify Python executable path from config used
6. Verify script path from config used
7. Verify error handling if script not found
8. Verify error handling if Python not installed
9. Verify error handling if script crashes
10. Verify execution logs captured for debugging

---

### Step 18: Photo Processing Pipeline
**Components**: Backend, all previous services

**Description**: Create orchestrator service that processes new/changed photos through complete pipeline.

**Pipeline Steps**:
1. Receive photo file path from Git polling service
2. Determine photo owner (from Git commit or default admin)
3. Extract EXIF data (Step 15)
4. Parse .metadata file if exists (Step 16)
5. Generate AI tags using STAG (Step 17)
6. Execute any file-extension-specific scripts (Step 19)
7. Generate thumbnail
8. Create Photo record in database
9. Create ExifData record
10. Create PhotoMetadata records
11. Create Tag records and PhotoTag associations
12. Log execution to script_execution_log

**Backend Implementation**:
- Create PhotoProcessingService with processPhoto(File photoFile, String commitAuthor) method
- Orchestrate all services
- Use @Transactional to ensure atomic database updates
- Handle partial failures gracefully

**Test Cases**:
1. Verify complete pipeline for photo with EXIF, metadata file, and STAG tags
2. Verify photo record created with correct attributes
3. Verify EXIF data saved correctly
4. Verify custom metadata saved correctly
5. Verify STAG tags saved correctly
6. Verify metadata file tags saved correctly
7. Verify thumbnail generated and path saved
8. Verify handling of photo without EXIF data
9. Verify handling of photo without .metadata file
10. Verify handling of STAG script failure (continue processing)
11. Verify transaction rollback on database error
12. Verify duplicate photo detection (same file_path)
13. Verify execution logged to script_execution_log

---

### Step 19: Custom Script Execution Engine
**Components**: Backend, scripts table, process execution

**Description**: Create service to execute custom scripts based on file extension mapping.

**Service Responsibilities**:
1. Maintain in-memory map of file extension → script ID
2. Reload map when scripts table updated
3. Execute appropriate script for given file
4. Handle scheduled scripts (daily run time)
5. Handle periodic scripts (recurring interval)
6. Log all executions

**Backend Implementation**:
- Create ScriptExecutionService
- Load scripts from database on startup
- For each script with file_extension: register in map
- For scripts with run_time: schedule daily using @Scheduled
- For scripts with periodicity: schedule recurring using @Scheduled
- Provide reloadScripts() method called from Edit Script Dialog

**Test Cases**:
1. Verify script map loaded from database on startup
2. Verify correct script selected based on file extension
3. Verify script executes successfully
4. Verify script output captured
5. Verify daily scheduled scripts run at correct time
6. Verify periodic scripts run at correct interval
7. Verify script execution logged to script_execution_log table
8. Verify script failures logged with error message
9. Verify reloadScripts() refreshes in-memory map
10. Verify multiple scripts can run concurrently
11. Verify script timeout prevents hanging
12. Verify scripts execute with correct working directory

---

### Step 20: Thumbnail Generation
**Components**: Backend, image processing library

**Description**: Create service to generate thumbnails for photos.

**Service Responsibilities**:
1. Accept photo file path as input
2. Generate thumbnail (max 200x200px, maintain aspect ratio)
3. Save thumbnail to configured directory
4. Return thumbnail file path

**Backend Implementation**:
- Create ThumbnailService with generateThumbnail(File photo) method
- Use Java ImageIO or Thumbnailator library
- Save thumbnails to /thumbnails subdirectory in Git repo
- Name thumbnails: {photo_id}_thumb.{ext}

**Test Cases**:
1. Verify thumbnail generated for JPG image
2. Verify thumbnail generated for PNG image
3. Verify thumbnail size max 200x200px
4. Verify aspect ratio maintained
5. Verify thumbnail saved to correct path
6. Verify thumbnail path returned correctly
7. Verify handling of corrupt image file
8. Verify handling of unsupported image format
9. Verify thumbnail quality acceptable
10. Verify thumbnail file size reasonable (< 50KB)

