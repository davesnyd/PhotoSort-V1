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

*Additional user functionality will be documented as features are implemented.*
