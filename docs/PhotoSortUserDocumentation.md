# PhotoSort User Documentation

## Overview
PhotoSort is a web-based photo management system that automatically discovers and indexes photos from a Git repository, extracts metadata, and provides search and viewing capabilities.

## Step 1: Database Configuration

### Functionality Created
**Database Setup and Configuration**

The database is the foundation of the PhotoSort system. It stores all information about users, photos, metadata, tags, and system configuration.

### System Setup (Administrator Only)

#### Prerequisites
- PostgreSQL 13 or higher installed
- Java 17 or higher installed
- Maven 3.8 or higher installed

#### Database Setup Steps
1. **Create the database**:
   - Open PostgreSQL command line or GUI tool
   - Create database: `CREATE DATABASE PhotoSortData;`
   - For testing: `CREATE DATABASE PhotoSortDataTest;`

2. **Set environment variables**:
   - `DB_USERNAME`: Your PostgreSQL username
   - `DB_PASSWORD`: Your PostgreSQL password

   Example (Linux/Mac):
   ```bash
   export DB_USERNAME=postgres
   export DB_PASSWORD=yourpassword
   ```

3. **Run the schema script**:
   - Navigate to PhotoSortServices/src/main/resources/
   - Execute schema.sql against PhotoSortData database

4. **Start the application**:
   ```bash
   cd PhotoSortServices
   mvn spring-boot:run
   ```

### Expected Results
- Application starts successfully
- Database connection established
- All tables created (users, photos, exif_data, etc.)
- No errors in console output

### Troubleshooting
- **Connection refused**: Verify PostgreSQL is running and credentials are correct
- **Database not found**: Ensure PhotoSortData database was created
- **Authentication failed**: Check DB_USERNAME and DB_PASSWORD environment variables

## Step 3: Database Connection Configuration

### Functionality Created
**Optimized Database Connection Management**

The application now uses advanced connection pooling and transaction management for improved performance and reliability.

### What This Means for Users

#### Performance Improvements
- **Faster response times**: Connection pooling eliminates the overhead of creating new database connections
- **Better concurrency**: Up to 10 simultaneous users can access the database efficiently
- **Batch operations**: Multiple database operations are grouped together for speed

#### Reliability Improvements
- **Connection health checks**: Bad connections are automatically detected and replaced
- **Automatic rollback**: If an error occurs, database changes are automatically undone
- **Connection timeout**: Prevents hanging when database is slow to respond

### Technical Details (For Advanced Users)

#### Connection Pool Configuration
The application uses HikariCP with these settings:
- Maximum connections: 10
- Minimum idle connections: 5
- Connection timeout: 30 seconds

To monitor connection pool health:
1. Check application logs for "HikariPool" entries
2. Look for connection acquisition times
3. Watch for connection timeout warnings

#### Transaction Management
All data modifications are wrapped in transactions:
- Changes are saved together or not at all
- Prevents partial updates that could corrupt data
- Automatically retries on temporary failures

### Troubleshooting

**Slow database operations**:
- Check if connection pool is exhausted (all 10 connections in use)
- Look for long-running transactions in application logs
- Consider increasing pool size if consistently hitting limit

**Transaction rollback errors**:
- Check for unique constraint violations (duplicate data)
- Verify foreign key relationships are valid
- Review application logs for specific error messages

**Connection pool exhaustion**:
- Error: "Connection is not available, request timed out after 30000ms"
- Solution: Ensure database is accessible and responsive
- Solution: Check for connection leaks (connections not being closed)

---

## Step 4: OAuth 2.0 Google Authentication

### Functionality Created
**Google Account Authentication**

PhotoSort uses Google OAuth 2.0 to securely authenticate users. You must have a Google account to access the system.

### Logging In

#### First-Time Login
1. Navigate to the PhotoSort application URL
2. Click the **"Sign in with Google"** button
3. You'll be redirected to Google's login page
4. Enter your Google credentials
5. Grant PhotoSort permission to access your basic profile information
6. You'll be redirected back to PhotoSort and logged in
7. Your user account is automatically created with "User" permissions

#### Subsequent Logins
1. Navigate to the PhotoSort application
2. Click **"Sign in with Google"**
3. If you're already logged into Google, you'll be automatically authenticated
4. You'll be taken to the PhotoSort home page

### User Types

#### User (Standard Permission)
- Can view and manage their own photos
- Can view public photos
- Can view photos they've been granted access to
- Cannot access administrative functions

#### Administrator
- Has all User permissions plus:
- Can manage all users
- Can manage all photos in the system
- Can configure system scripts
- Can modify system configuration
- Can promote other users to Administrator status

### Logging Out
1. Click your name in the top-right corner of the navigation bar
2. Click the **"Logout"** button
3. You'll be logged out and redirected to the login page

### Troubleshooting

**Can't log in**:
- Ensure you have an active Google account
- Check that your browser allows cookies
- Try clearing your browser cache
- Verify the application URL is correct

**Permission denied errors**:
- You're trying to access administrator-only features
- Contact your system administrator to request elevated permissions

**Session expired**:
- Your session times out after a period of inactivity
- Simply log in again to continue

---

## Step 5: React Frontend Application

### Functionality Created
**Web-Based User Interface**

PhotoSort now has a modern, responsive web interface built with React. You can access all PhotoSort features through your web browser.

### Accessing the Application

#### Starting the Frontend
For development:
```bash
cd photosort-frontend
npm start
```
The application will open in your browser at `http://localhost:3000`

For production:
```bash
cd photosort-frontend
npm run build
```
Serve the build folder with any static web server.

### User Interface Components

#### Login Page
- **Location**: `http://localhost:3000/login`
- **Elements**:
  - PhotoSort logo and title
  - "Sign in with Google" button with Google logo
  - Instructions for logging in
- **Usage**:
  - Click the Google sign-in button to authenticate
  - You'll be redirected to Google's authentication page
  - After successful login, you'll be taken to the home page

#### Navigation Bar (After Login)
Located at the top of every page with navy blue background:

**For All Users**:
- **PhotoSort** (logo/brand) - Click to return to home page
- **Home** - Main dashboard
- **My Photos** - Your personal photo library (placeholder for future)
- Your name - Displays current user
- **Logout** button - Sign out of the application

**For Administrators**:
Additional menu items:
- **Users** - Manage all users (placeholder for Step 6)
- **Photos** - Manage all photos (placeholder for Step 7)
- **Scripts** - Configure automated scripts (placeholder for Step 11)
- **Configuration** - System settings (placeholder for Step 13)

#### Home Page
- **Welcome message**: Personalized greeting with your name
- **Feature cards**: Overview of PhotoSort capabilities
  - Photo Management
  - Smart Tagging
  - EXIF Data
  - Search & Filter

### Color Scheme
PhotoSort uses a consistent color palette:
- **Primary** (Burgundy): #800020 - Used for headings and emphasis
- **Secondary** (Navy Blue): #000080 - Used for navigation and borders
- **Accent** (Cream): #FFFDD0 - Used for backgrounds and text

### Browser Requirements
PhotoSort supports the latest versions of:
- Google Chrome
- Mozilla Firefox
- Safari
- Microsoft Edge

### Responsive Design
- Optimized for desktop browsers (1024px and wider)
- Tablet support for screens 768px and wider
- Mobile support is planned for future releases

### Troubleshooting

**Application won't start**:
- Ensure Node.js 18+ is installed: `node --version`
- Install dependencies: `npm install`
- Check that port 3000 is not in use
- Review console for error messages

**Can't connect to backend**:
- Verify backend is running on port 8080
- Check the `.env` file has correct `REACT_APP_API_BASE_URL`
- Look for CORS errors in browser console

**Blank page after login**:
- Check browser console for JavaScript errors
- Verify authentication was successful
- Try logging out and back in
- Clear browser cache and cookies

**Build errors**:
- Delete `node_modules` folder and `package-lock.json`
- Run `npm install` again
- Ensure all dependencies are compatible

---

## Step 6: Admin Navigation and User Table Page

### Functionality Created
**User Management Interface**

Administrators can now view all users, search for specific users, manage user permissions, and view photo counts for each user.

### Accessing User Management

**For Administrators Only**:
1. Log in to PhotoSort
2. Click **"Users"** in the navigation bar
3. You'll see the User Management page with a table of all users

### User Management Page Elements

#### Search Functionality
**Quick Search**:
- **Text input field**: Type to search for users by email or display name
- **Search button**: Click to execute the search
- Search is case-insensitive and matches partial text

**Advanced Search**:
- **Column dropdown**: Select which field to search (email, displayName, userType)
- **Filter type**: Choose "Must Contain" or "Must Not Contain"
- **Value input**: Enter the search term
- Can combine multiple filters for precise results

#### User Table
Displays all users with the following columns:
- **ID**: Unique user identifier
- **Email**: User's Google account email
- **Display Name**: User's full name from Google
- **Type**: USER or ADMIN (shown as colored badge)
- **Photos**: Number of photos owned by this user
- **First Login**: Date and time of initial login
- **Last Login**: Most recent login timestamp

#### Sorting
- Click any column header to sort by that column
- Click again to reverse sort order (ascending/descending)
- Current sort column is highlighted

#### Actions
For each user row:
- **Edit button**: Change user type between USER and ADMIN
  - Click Edit to show dropdown
  - Select new type (USER or ADMIN)
  - Click Save to apply or Cancel to discard
- **View Images button**: Navigate to view all photos for that user

#### Pagination
- **Page controls**: Navigate between pages of users
- **Items per page**: 10 users shown per page
- **Total count**: Shows how many users match your search/filters

### Common Tasks

#### Promoting a User to Administrator
1. Navigate to Users page
2. Find the user (use search if needed)
3. Click the **Edit** button for that user
4. Select **ADMIN** from the dropdown
5. Click **Save**
6. User now has admin privileges

#### Finding Users with Specific Criteria
Example: Find all administrators
1. Click **Advanced Search**
2. Select column: **userType**
3. Select filter: **Must Contain**
4. Enter value: **ADMIN**
5. Click Search
6. Only admin users will be displayed

#### Viewing a User's Photos
1. Find the user in the table
2. Click **View Images** for that user
3. You'll be taken to the Photos page filtered for that user

### Troubleshooting

**Can't see the Users menu**:
- You must be an Administrator to access user management
- Contact an existing administrator to upgrade your permissions

**Changes not saved**:
- Ensure you clicked the Save button, not just selected a value
- Check for error messages at the top of the page
- Verify you have a stable internet connection

**Search returns no results**:
- Try a more general search term
- Check spelling of search text
- Clear filters and try again

---

## Step 7: Photo Table Page

### Functionality Created
**Photo Management Interface with Permission-Based Viewing**

All users can now view and manage photos they have access to. The system automatically filters photos based on ownership and permissions.

### Accessing Photos

**View Your Own Photos**:
1. Log in to PhotoSort
2. Click **"My Photos"** in the navigation bar
3. You'll see all photos you own, public photos, and photos you've been granted access to

**View All Photos (Administrators Only)**:
1. Log in as an Administrator
2. Click **"Photos"** in the admin navigation
3. You'll see all photos in the system regardless of owner

**View a Specific User's Photos (Administrators Only)**:
1. Go to the Users page
2. Find the user
3. Click **"View Images"** for that user
4. You'll see all photos owned by that user

### Photo Table Page Elements

#### Search Functionality
**Quick Search**:
- **Text input field**: Type to search by filename or file path
- **Search button**: Execute the search
- Finds photos matching the text in filename or path

**Advanced Search**:
- **Column dropdown**: Select field to filter (fileName, filePath, fileSize, etc.)
- **Filter type**: "Must Contain" or "Must Not Contain"
- **Value**: Enter search term
- **Multiple filters**: Can combine up to 2 filters simultaneously
- Example: Find all photos with "vacation" in the name but not "2023"

#### Photo Table Columns
- **Thumbnail**: Small preview of the photo (80x80 pixels)
  - Shows "No Image" if thumbnail unavailable
  - Click View button to see full-size image
- **File Name**: Original filename of the photo
- **Size**: File size in KB, MB, or GB
- **Dimensions**: Width × Height in pixels
- **Created**: Date and time the photo was created (from file metadata)
- **Owner**: Display name of the user who owns this photo
- **Public**: Badge showing "Public" (green) or "Private" (burgundy)
  - Public photos are visible to all users
  - Private photos only visible to owner and users with granted permission

#### Sorting
- Click column headers to sort (not available for Thumbnail or Dimensions)
- Toggles between ascending and descending order
- Current sort column is highlighted

#### Actions
**View Button**:
- Opens the full-size image in a new browser tab
- Preserves original quality
- Allows browser zoom and download options

#### Pagination
- **Page controls**: Navigate between pages
- **Items per page**: 10 photos shown per page
- **Total count**: Shows total accessible photos

### Permission System

#### Who Can See Which Photos?

**Regular Users See**:
1. **Their own photos** (both public and private)
2. **All public photos** from any user
3. **Private photos** they've been explicitly granted access to

**Administrators See**:
- **All photos** in the system regardless of owner or visibility
- Can filter by specific user ID to view that user's photos only

#### Privacy Implications
- Private photos remain private unless explicitly shared
- Owners can change photo visibility (future feature)
- Administrators have full visibility for system management purposes

### Common Tasks

#### Finding Photos from a Vacation
1. Navigate to Photos page
2. Enter "vacation" in Quick Search
3. Click Search
4. All photos with "vacation" in filename or path will appear

#### Finding Large Photos
1. Click Advanced Search
2. Select column: **fileSize**
3. Select filter: **Must Contain**
4. Enter value: **5000000** (for ~5MB+)
5. Click Search

#### Viewing Full-Size Image
1. Find the photo in the table
2. Click the **View** button
3. Image opens in new tab
4. Use browser controls to zoom, download, or print

#### Finding Photos by Owner
(Administrators only)
1. Note the owner's name in the table
2. Or go to Users page and click **View Images** for that user
3. Photos are automatically filtered by owner

### Troubleshooting

**No photos appear**:
- If you're a regular user, you may not have any photos yet
- Check that you own photos or have been granted access
- Try clearing search filters
- Administrators: verify photos exist in the database

**Thumbnail shows "No Image"**:
- Thumbnail file may not have been generated yet
- Original file path may be incorrect
- Click View to see if full image is available

**Can't see another user's private photos**:
- This is expected behavior for privacy
- Only the owner and users with explicit permission can see private photos
- Administrators can see all photos

**Search returns unexpected results**:
- Quick search matches both filename AND path
- Try more specific search terms
- Use Advanced Search for precise filtering

**Slow loading with many photos**:
- Pagination limits to 10 photos per page for performance
- Use search/filters to narrow results
- Thumbnails are optimized for fast loading

---

*Additional user functionality will be documented as features are implemented.*
