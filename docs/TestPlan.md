# Test Plan

This document outlines manual and automated test cases for the PhotoSort application.

## Step 1: Database Configuration

### Test Case: Database Connection
1. **Name**: Database Connection Test
2. **Functionality Tested**: Connection to PostgreSQL database using environment variables
3. **Steps Required**:
   - Set environment variables DB_USERNAME and DB_PASSWORD
   - Start the Spring Boot application
   - Verify application starts without database connection errors
4. **Expected Outcome**: Application connects successfully to PhotoSortData database

### Test Case: User CRUD Operations
1. **Name**: User CRUD Operations Test
2. **Functionality Tested**: Create, Read, Update, Delete operations on User entity
3. **Steps Required**:
   - Run DatabaseSchemaTest.testInsertUser()
   - Verify user is created with all fields
   - Query user by Google ID and email
   - Update user type
   - Delete user
4. **Expected Outcome**: All CRUD operations succeed without errors

### Test Case: Photo Foreign Key Relationship
1. **Name**: Photo Foreign Key Test
2. **Functionality Tested**: Foreign key relationship between Photo and User
3. **Steps Required**:
   - Create a user
   - Create a photo associated with that user
   - Query photos by owner
   - Verify relationship integrity
4. **Expected Outcome**: Photo correctly references user, queries return expected results

### Test Case: Cascade Delete
1. **Name**: Cascade Delete Test
2. **Functionality Tested**: Cascade delete from Photo to EXIF data
3. **Steps Required**:
   - Create a photo with EXIF data
   - Delete the photo
   - Verify EXIF data is also deleted
4. **Expected Outcome**: EXIF data is automatically deleted when photo is deleted

### Test Case: Unique Constraints
1. **Name**: Unique Constraint Test
2. **Functionality Tested**: Unique constraints on user Google ID, email, and photo file path
3. **Steps Required**:
   - Create a user
   - Attempt to create another user with same Google ID
   - Verify exception is thrown
4. **Expected Outcome**: Database rejects duplicate entries, exception is thrown

### Test Case: Metadata Field and Photo Metadata
1. **Name**: Metadata Operations Test
2. **Functionality Tested**: Custom metadata field creation and photo metadata association
3. **Steps Required**:
   - Create metadata field (e.g., "Location")
   - Create photo metadata linking photo to field with value
   - Query metadata for photo
4. **Expected Outcome**: Metadata correctly associated with photo, queryable

### Test Case: Tag Operations
1. **Name**: Tag Operations Test
2. **Functionality Tested**: Tag creation and photo-tag association
3. **Steps Required**:
   - Create tag (e.g., "nature")
   - Associate tag with photo
   - Query tags for photo
4. **Expected Outcome**: Tags correctly associated with photos, queryable

### Test Case: Photo Permissions
1. **Name**: Photo Permissions Test
2. **Functionality Tested**: Photo access permission management
3. **Steps Required**:
   - Create two users
   - Create a photo owned by user 1
   - Grant user 2 permission to access photo
   - Verify permission exists
4. **Expected Outcome**: Permission is created, user 2 can access private photo

### Test Case: Script Management
1. **Name**: Script Management Test
2. **Functionality Tested**: Script creation and configuration
3. **Steps Required**:
   - Create script with file extension mapping
   - Create script with periodic execution
   - Query scripts by file extension
4. **Expected Outcome**: Scripts are created and queryable by various criteria

### Test Case: Script Execution Logging
1. **Name**: Script Execution Log Test
2. **Functionality Tested**: Logging of script executions
3. **Steps Required**:
   - Create script
   - Create execution log for script
   - Query logs by script and status
4. **Expected Outcome**: Execution logs are created and queryable

## Step 3: Database Connection Configuration

### Test Case: Database Connection Successful
1. **Name**: Database Connection Test
2. **Functionality Tested**: Connection to PostgreSQL database via HikariCP
3. **Steps Required**:
   - Start application
   - Verify DataSource is created
   - Obtain connection from pool
   - Check connection metadata
4. **Expected Outcome**: Connection established to PostgreSQL database

### Test Case: Entity Mapping Validation
1. **Name**: Entity Mapping Test
2. **Functionality Tested**: All entity classes correctly map to database tables
3. **Steps Required**:
   - Persist instance of each entity type
   - Verify auto-generated IDs
   - Verify all fields saved correctly
4. **Expected Outcome**: All 11 entity types can be persisted without errors

### Test Case: CRUD Operations with Foreign Keys
1. **Name**: Foreign Key CRUD Test
2. **Functionality Tested**: CRUD operations maintaining referential integrity
3. **Steps Required**:
   - Create user
   - Create photo with foreign key to user
   - Update photo
   - Delete photo
   - Verify foreign key maintained throughout
4. **Expected Outcome**: All CRUD operations succeed with valid foreign keys

### Test Case: Cascade Delete Validation
1. **Name**: Cascade Delete Test
2. **Functionality Tested**: CASCADE DELETE on dependent entities
3. **Steps Required**:
   - Create photo with EXIF data
   - Delete photo
   - Verify EXIF data automatically deleted
4. **Expected Outcome**: Child entities deleted when parent is deleted

### Test Case: SQL Query Generation
1. **Name**: Hibernate SQL Generation Test
2. **Functionality Tested**: Hibernate generates correct SQL for operations
3. **Steps Required**:
   - Enable SQL logging
   - Perform save and query operations
   - Verify SQL appears in logs
   - Check SQL syntax is correct
4. **Expected Outcome**: Valid PostgreSQL SQL generated for all operations

### Test Case: Transaction Rollback
1. **Name**: Transaction Rollback Test
2. **Functionality Tested**: Transaction rollback on constraint violation
3. **Steps Required**:
   - Save valid entity
   - Attempt to save duplicate (violates unique constraint)
   - Verify exception thrown
   - Verify database state unchanged
4. **Expected Outcome**: Transaction rolls back, no partial data saved

## Step 4: OAuth 2.0 Google Authentication

### Test Case: First Time User Login
1. **Name**: New User OAuth Login Test
2. **Functionality Tested**: Creating new user account on first OAuth login
3. **Steps Required**:
   - Simulate OAuth login with new Google ID
   - Verify user created in database
   - Check user type is USER (default)
   - Verify first login date and last login date are set
4. **Expected Outcome**: New user record created with correct attributes

### Test Case: Returning User Login
1. **Name**: Existing User OAuth Login Test
2. **Functionality Tested**: Updating last login date for returning users
3. **Steps Required**:
   - Create user with past login date
   - Simulate OAuth login with same Google ID
   - Verify last login date is updated
   - Verify first login date remains unchanged
4. **Expected Outcome**: Last login date updated, first login preserved

### Test Case: Find User by Google ID
1. **Name**: Find User by Google ID Test
2. **Functionality Tested**: User lookup by OAuth identifier
3. **Steps Required**:
   - Create user with Google ID
   - Call findByGoogleId method
   - Verify correct user returned
4. **Expected Outcome**: User found and returned correctly

### Test Case: Update User Type
1. **Name**: User Type Update Test
2. **Functionality Tested**: Changing user from USER to ADMIN
3. **Steps Required**:
   - Create regular user
   - Update user type to ADMIN
   - Verify user type changed in database
   - Check isAdmin returns true
4. **Expected Outcome**: User type successfully updated

### Test Case: Admin Status Check
1. **Name**: Admin Status Check Test
2. **Functionality Tested**: Determining if user is administrator
3. **Steps Required**:
   - Create regular user
   - Check isAdmin (should be false)
   - Promote to admin
   - Check isAdmin (should be true)
4. **Expected Outcome**: Admin status correctly reflects user type

### Test Case: OAuth Configuration Loading
1. **Name**: OAuth Configuration Test
2. **Functionality Tested**: Loading OAuth credentials from environment
3. **Steps Required**:
   - Set OAUTH_CLIENT_ID environment variable
   - Set OAUTH_CLIENT_SECRET environment variable
   - Start application
   - Verify OAuth configuration loaded
4. **Expected Outcome**: OAuth client configured successfully

### Test Case: Unauthenticated API Access
1. **Name**: Unauthenticated Request Test
2. **Functionality Tested**: Blocking unauthenticated requests to protected endpoints
3. **Steps Required**:
   - Make request to /api/auth/current without authentication
   - Verify 401 status returned
4. **Expected Outcome**: Access denied with 401 Unauthorized

### Test Case: Authenticated API Access
1. **Name**: Authenticated Request Test
2. **Functionality Tested**: Allowing authenticated requests with user context
3. **Steps Required**:
   - Authenticate user via OAuth
   - Make request to /api/auth/current
   - Verify user information returned
4. **Expected Outcome**: User data returned with 200 OK

### Test Case: Session Management
1. **Name**: Session Timeout Test
2. **Functionality Tested**: Session expiration after configured timeout
3. **Steps Required**:
   - Login user
   - Wait for session timeout (30 minutes)
   - Make request to protected endpoint
   - Verify session expired
4. **Expected Outcome**: Session expires, authentication required again

### Test Case: CSRF Protection
1. **Name**: CSRF Protection Test
2. **Functionality Tested**: CSRF token validation on state-changing requests
3. **Steps Required**:
   - Make POST request without CSRF token
   - Verify request rejected
   - Make POST request with valid CSRF token
   - Verify request accepted
4. **Expected Outcome**: CSRF protection enforced correctly

---

## Step 7: Photo Table Page

### Test Case: User Views Authorized Photos
1. **Name**: Regular User Permission Filtering
2. **Functionality Tested**: Users can only see photos they own, public photos, or photos they have explicit permission for
3. **Steps Required**:
   - Login as regular user (USER type)
   - Click "My Photos" in navigation
   - Observe photos displayed
   - Verify table shows photo details (thumbnail, name, size, dimensions, created date, owner, public/private status)
4. **Expected Outcome**: 
   - User sees their own photos
   - User sees public photos from other users
   - User sees private photos they've been granted access to
   - User does NOT see private photos they don't have access to

### Test Case: Admin Views All Photos
1. **Name**: Administrator Full Access
2. **Functionality Tested**: Administrators can see all photos regardless of ownership or permissions
3. **Steps Required**:
   - Login as administrator (ADMIN type)
   - Click "Photos" in admin navigation
   - Observe all photos displayed
   - Verify photos from multiple users are visible
4. **Expected Outcome**: All photos in the system are displayed, including private photos from other users

### Test Case: Photo Quick Search
1. **Name**: Search Photos by Filename or Path
2. **Functionality Tested**: Quick search filters photos by matching text in filename or file path
3. **Steps Required**:
   - Navigate to Photos page
   - Enter search term in quick search field (e.g., "vacation")
   - Click Search button
   - Observe filtered results
4. **Expected Outcome**: Only photos with "vacation" in filename or path are displayed

### Test Case: Photo Advanced Search
1. **Name**: Multi-Filter Photo Search
2. **Functionality Tested**: Advanced search with multiple filter conditions
3. **Steps Required**:
   - Navigate to Photos page
   - Click Advanced Search
   - Set Filter 1: Field = "fileName", Type = "Must Contain", Value = "beach"
   - Set Filter 2: Field = "fileName", Type = "Must Not Contain", Value = "2023"
   - Click Search
4. **Expected Outcome**: Only photos with "beach" in filename but NOT containing "2023" are shown

### Test Case: Photo Table Sorting
1. **Name**: Sort Photos by Column
2. **Functionality Tested**: Clicking column headers sorts photos
3. **Steps Required**:
   - Navigate to Photos page
   - Click "File Name" column header
   - Observe photos sorted alphabetically ascending
   - Click "File Name" header again
   - Observe photos sorted alphabetically descending
   - Try sorting by other columns (Size, Created, Owner)
4. **Expected Outcome**: Photos sort correctly by selected column, toggles between ascending/descending

### Test Case: Photo Pagination
1. **Name**: Navigate Through Pages of Photos
2. **Functionality Tested**: Pagination controls work correctly
3. **Steps Required**:
   - Navigate to Photos page (assuming >10 photos exist)
   - Observe 10 photos displayed on page 1
   - Note total pages and total photos count
   - Click "Next" button
   - Observe next 10 photos displayed
   - Click "Previous" button
   - Click specific page number
4. **Expected Outcome**: 
   - 10 photos per page
   - Pagination controls navigate correctly
   - Total counts are accurate

### Test Case: View Full-Size Photo
1. **Name**: Open Photo in New Tab
2. **Functionality Tested**: View button opens full-size image
3. **Steps Required**:
   - Navigate to Photos page
   - Find a photo in the table
   - Click "View" button for that photo
   - Observe new browser tab opens
4. **Expected Outcome**: Full-size image opens in new tab, original quality preserved

### Test Case: Photo Thumbnail Display
1. **Name**: Thumbnail Preview in Table
2. **Functionality Tested**: Thumbnails display correctly or show placeholder
3. **Steps Required**:
   - Navigate to Photos page
   - Observe Thumbnail column
   - Verify thumbnails load for photos with valid paths
   - Verify "No Image" placeholder for photos without thumbnails
4. **Expected Outcome**: Thumbnails display at 80x80 pixels, broken images show "No Image" placeholder

### Test Case: Public vs Private Badge
1. **Name**: Visibility Status Display
2. **Functionality Tested**: Public/Private badge shows photo visibility
3. **Steps Required**:
   - Navigate to Photos page
   - Observe "Public" column
   - Verify public photos have green "Public" badge
   - Verify private photos have burgundy "Private" badge
4. **Expected Outcome**: Badges correctly reflect photo visibility settings

### Test Case: Admin Filter by User
1. **Name**: Administrator View User's Photos
2. **Functionality Tested**: Admin can filter photos by specific user
3. **Steps Required**:
   - Login as administrator
   - Navigate to Users page
   - Find a user in the table
   - Click "View Images" button for that user
   - Observe Photos page opens filtered for that user
4. **Expected Outcome**: Only photos owned by selected user are displayed

### Test Case: File Size Formatting
1. **Name**: Human-Readable File Sizes
2. **Functionality Tested**: File sizes display in appropriate units
3. **Steps Required**:
   - Navigate to Photos page
   - Observe "Size" column
   - Check photos of varying sizes
4. **Expected Outcome**: 
   - Small files show in KB (e.g., "123.4 KB")
   - Medium files show in MB (e.g., "5.2 MB")
   - Large files show in GB (e.g., "1.3 GB")

### Test Case: Photo Owner Display
1. **Name**: Owner Information Shown
2. **Functionality Tested**: Each photo shows owner's display name
3. **Steps Required**:
   - Navigate to Photos page
   - Observe "Owner" column
   - Verify owner names are displayed
4. **Expected Outcome**: Each photo shows the display name of the user who owns it

### Test Case: Date Formatting
1. **Name**: Created Date Display
2. **Functionality Tested**: File creation dates formatted for readability
3. **Steps Required**:
   - Navigate to Photos page
   - Observe "Created" column
   - Check date format
4. **Expected Outcome**: Dates display in format like "Nov 30, 2024, 02:30 PM"

### Test Case: Empty State
1. **Name**: No Photos Available Message
2. **Functionality Tested**: Appropriate message when no photos match criteria
3. **Steps Required**:
   - Login as new user with no photos
   - Navigate to Photos page
   - OR: Perform search with no matching results
4. **Expected Outcome**: "No photos found" message displayed

### Test Case: Loading State
1. **Name**: Loading Indicator During API Call
2. **Functionality Tested**: Loading message shows while fetching photos
3. **Steps Required**:
   - Navigate to Photos page
   - Observe briefly before photos load
4. **Expected Outcome**: "Loading photos..." message appears, then photos display once loaded

### Test Case: Error Handling
1. **Name**: API Error Display
2. **Functionality Tested**: Error messages shown on API failures
3. **Steps Required**:
   - Stop backend server
   - Navigate to Photos page or perform search
   - Observe error handling
4. **Expected Outcome**: User-friendly error message displayed (not raw error)
