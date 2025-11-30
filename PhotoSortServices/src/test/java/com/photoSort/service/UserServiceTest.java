/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.photoSort.model.User;
import com.photoSort.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for UserService - OAuth authentication and user management.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(UserService.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final String GOOGLE_ID = "google123";
    private static final String EMAIL = "test@example.com";
    private static final String DISPLAY_NAME = "Test User";

    @BeforeEach
    public void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
    }

    // Test Case 1: Create new user record on first login
    @Test
    public void testProcessOAuthLogin_NewUser() {
        // Process OAuth login for new user
        User user = userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);

        assertNotNull(user);
        assertNotNull(user.getUserId());
        assertEquals(GOOGLE_ID, user.getGoogleId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(DISPLAY_NAME, user.getDisplayName());
        assertEquals(User.UserType.USER, user.getUserType());
        assertNotNull(user.getFirstLoginDate());
        assertNotNull(user.getLastLoginDate());

        // Verify user was saved to database
        Optional<User> savedUser = userRepository.findByGoogleId(GOOGLE_ID);
        assertTrue(savedUser.isPresent());
    }

    // Test Case 2: Update existing user's last login date on subsequent logins
    @Test
    public void testProcessOAuthLogin_ExistingUser() {
        // Create initial user
        User initialUser = new User();
        initialUser.setGoogleId(GOOGLE_ID);
        initialUser.setEmail(EMAIL);
        initialUser.setDisplayName(DISPLAY_NAME);
        initialUser.setUserType(User.UserType.USER);
        LocalDateTime firstLogin = LocalDateTime.now().minusDays(1);
        initialUser.setFirstLoginDate(firstLogin);
        initialUser.setLastLoginDate(firstLogin);
        userRepository.save(initialUser);

        // Simulate subsequent login
        User returnedUser = userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);

        assertNotNull(returnedUser);
        assertEquals(initialUser.getUserId(), returnedUser.getUserId());
        assertEquals(firstLogin, returnedUser.getFirstLoginDate()); // First login should not change
        assertTrue(returnedUser.getLastLoginDate().isAfter(firstLogin)); // Last login should be updated
    }

    // Test Case 3: Find user by Google ID
    @Test
    public void testFindByGoogleId() {
        // Create user
        userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);

        // Find by Google ID
        Optional<User> found = userService.findByGoogleId(GOOGLE_ID);

        assertTrue(found.isPresent());
        assertEquals(GOOGLE_ID, found.get().getGoogleId());
    }

    // Test Case 4: Find user by email
    @Test
    public void testFindByEmail() {
        // Create user
        userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);

        // Find by email
        Optional<User> found = userService.findByEmail(EMAIL);

        assertTrue(found.isPresent());
        assertEquals(EMAIL, found.get().getEmail());
    }

    // Test Case 5: Update user type
    @Test
    public void testUpdateUserType() {
        // Create user
        User user = userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);
        assertEquals(User.UserType.USER, user.getUserType());

        // Update to admin
        User updated = userService.updateUserType(user.getUserId(), User.UserType.ADMIN);

        assertEquals(User.UserType.ADMIN, updated.getUserType());

        // Verify in database
        Optional<User> fromDb = userRepository.findById(user.getUserId());
        assertTrue(fromDb.isPresent());
        assertEquals(User.UserType.ADMIN, fromDb.get().getUserType());
    }

    // Test Case 6: Check if user is admin
    @Test
    public void testIsAdmin() {
        // Create regular user
        User user = userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);
        assertFalse(userService.isAdmin(user.getUserId()));

        // Make user admin
        userService.updateUserType(user.getUserId(), User.UserType.ADMIN);
        assertTrue(userService.isAdmin(user.getUserId()));
    }

    // Test Case 7: Handle non-existent user ID
    @Test
    public void testUpdateUserType_NonExistentUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserType(999L, User.UserType.ADMIN);
        });
    }

    // Test Case 8: Check admin status for non-existent user
    @Test
    public void testIsAdmin_NonExistentUser() {
        assertFalse(userService.isAdmin(999L));
    }

    // Test Case 9: Verify first and last login dates are set correctly
    @Test
    public void testLoginDates() {
        LocalDateTime beforeLogin = LocalDateTime.now();

        User user = userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);

        LocalDateTime afterLogin = LocalDateTime.now();

        assertNotNull(user.getFirstLoginDate());
        assertNotNull(user.getLastLoginDate());
        assertTrue(user.getFirstLoginDate().isAfter(beforeLogin) || user.getFirstLoginDate().isEqual(beforeLogin));
        assertTrue(user.getLastLoginDate().isBefore(afterLogin) || user.getLastLoginDate().isEqual(afterLogin));
        // First and last login should be within 1 second of each other for first login
        assertTrue(Math.abs(java.time.Duration.between(user.getFirstLoginDate(), user.getLastLoginDate()).toMillis()) < 1000);
    }

    // Test Case 10: Verify user attributes are preserved
    @Test
    public void testUserAttributesPreserved() {
        // Create user
        User user = userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);

        // Login again
        User secondLogin = userService.processOAuthLogin(GOOGLE_ID, EMAIL, DISPLAY_NAME);

        // User ID should be the same
        assertEquals(user.getUserId(), secondLogin.getUserId());

        // All attributes should be preserved
        assertEquals(user.getGoogleId(), secondLogin.getGoogleId());
        assertEquals(user.getEmail(), secondLogin.getEmail());
        assertEquals(user.getDisplayName(), secondLogin.getDisplayName());
        assertEquals(user.getUserType(), secondLogin.getUserType());
        assertEquals(user.getFirstLoginDate(), secondLogin.getFirstLoginDate());
    }
}
