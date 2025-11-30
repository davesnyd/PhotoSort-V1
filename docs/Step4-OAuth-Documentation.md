# Step 4: OAuth 2.0 Google Authentication - Documentation

## Developer Documentation

### Functionality Created
**Google OAuth 2.0 Authentication with User Management**

This step implements secure authentication using Google OAuth 2.0, integrating with the PhotoSort user model.

### Implementation Details

#### Security Configuration (SecurityConfig.java)
- Spring Security with OAuth 2.0 client support
- Endpoint protection (public vs authenticated)
- CSRF protection with cookie-based token repository
- Session management (30-minute timeout)
- Logout handling

**Key Features**:
- Public endpoints: /, /login, /error, /oauth2/**
- Protected endpoints: /api/** (require authentication)
- OAuth login page: /login
- Success redirect: / (home page)
- Failure redirect: /login?error=true

#### Custom OAuth2 User Service (CustomOAuth2UserService.java)
Extends `DefaultOAuth2UserService` to integrate Google OAuth with PhotoSort users.

**Process Flow**:
1. User initiates OAuth login
2. Google authentication completes
3. `loadUser()` called with OAuth user info
4. Extract Google ID, email, and name
5. Call `UserService.processOAuthLogin()`
6. Return `CustomOAuth2User` with PhotoSort user data

**Custom Attributes**:
- `userId`: PhotoSort internal user ID
- `userType`: USER or ADMIN
- All standard OAuth attributes (email, name, etc.)

#### User Service (UserService.java)
Manages user lifecycle and authentication business logic.

**Key Methods**:
- `processOAuthLogin()`: Create new user or update last login
- `findByGoogleId()`: Lookup user by OAuth ID
- `updateUserType()`: Promote user to admin
- `isAdmin()`: Check admin status

**Business Rules**:
- New users default to USER type
- First login sets both first_login_date and last_login_date
- Subsequent logins only update last_login_date
- Google ID is unique identifier for OAuth users

#### Authentication Controller (AuthController.java)
REST API endpoints for authentication status and user information.

**Endpoints**:
- `GET /api/auth/current`: Get current authenticated user
- `GET /api/auth/status`: Check authentication status

**Response Format**:
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "email": "user@example.com",
    "displayName": "User Name",
    "userType": "USER",
    "isAdmin": false
  }
}
```

### Configuration

**Environment Variables Required**:
```bash
export OAUTH_CLIENT_ID="your-google-client-id"
export OAUTH_CLIENT_SECRET="your-google-client-secret"
export DB_USERNAME="your-db-username"
export DB_PASSWORD="your-db-password"
```

**Google Cloud Console Setup**:
1. Create OAuth 2.0 credentials
2. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
3. Copy client ID and client secret to environment variables

### Testing

**UserServiceTest.java** - 10 test cases:
1. Create new user on first login
2. Update last login for returning users
3. Find user by Google ID
4. Find user by email
5. Update user type (USER to ADMIN)
6. Check admin status
7. Handle non-existent user
8. Verify login dates set correctly
9. Preserve user attributes across logins
10. Handle edge cases

### Security Considerations

**CSRF Protection**:
- Enabled for all state-changing requests
- Cookie-based token repository
- Tokens are HttpOnly (when secure flag is true)

**Session Security**:
- 30-minute timeout
- HttpOnly cookies
- Secure flag (should be true in production)
- Session invalidation on logout

**OAuth Security**:
- Client secret stored in environment variable
- State parameter prevents CSRF in OAuth flow
- Redirect URI validation

### Limitations

- Currently only supports Google OAuth (not GitHub, Facebook, etc.)
- No refresh token handling (user must re-authenticate after session expires)
- No remember-me functionality
- Admin promotion requires manual database update or admin UI (future step)

### Future Enhancements

- Multi-provider OAuth (GitHub, Facebook)
- Refresh token support for extended sessions
- Remember-me functionality
- Admin UI for user management
- Role-based access control (RBAC) beyond USER/ADMIN
- OAuth scope customization

---

## User Documentation

### Functionality Created
**Google Account Sign-In**

PhotoSort now supports secure login using your Google account. No password required!

### How to Sign In

1. Navigate to the PhotoSort application
2. Click "Sign in with Google" button
3. Choose your Google account
4. Grant permissions to PhotoSort (first time only)
5. You're logged in!

### First Time Users

On your first login:
- A PhotoSort account is automatically created
- Your Google email and name are used
- You start as a regular user (not administrator)
- Your first login date is recorded

### Returning Users

On subsequent logins:
- Your last login date is updated
- All your photos and settings are preserved
- You're automatically recognized by your Google account

### Session and Security

**Session Duration**:
- You remain logged in for 30 minutes of inactivity
- Active use extends your session
- After timeout, you'll need to sign in again

**Privacy**:
- Only your email and name are accessed from Google
- PhotoSort doesn't have access to other Google services
- You can revoke access anytime from Google Account settings

**Logging Out**:
- Click "Logout" in the navigation menu
- Your session is terminated immediately
- You'll be redirected to the login page

### User Types

**Regular User**:
- Can upload and manage your own photos
- Can view public photos
- Can view private photos you've been granted access to

**Administrator**:
- All regular user permissions
- Can manage all photos in the system
- Can manage user accounts
- Can configure system settings

Note: Contact an existing administrator to be promoted to admin status.

### Troubleshooting

**"OAuth error" message**:
- Check that application has been configured with valid Google OAuth credentials
- Ensure redirect URI matches OAuth configuration
- Try logging out and back in

**Session expired**:
- This is normal after 30 minutes of inactivity
- Simply sign in again to continue

**Can't access certain features**:
- You may need administrator privileges
- Contact an administrator for access

