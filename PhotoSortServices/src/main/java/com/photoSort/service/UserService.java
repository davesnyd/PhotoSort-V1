/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.photoSort.model.User;
import com.photoSort.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing user authentication and user-related operations.
 * Handles OAuth user creation and login tracking.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Process OAuth login. Creates new user on first login or updates last login date
     * for returning users.
     *
     * @param googleId     Google OAuth user ID
     * @param email        User's email address
     * @param displayName  User's display name
     * @return The user (newly created or existing)
     */
    public User processOAuthLogin(String googleId, String email, String displayName) {
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            // Returning user - update last login date
            User user = existingUser.get();
            user.setLastLoginDate(LocalDateTime.now());
            return userRepository.save(user);
        } else {
            // New user - create account
            User newUser = new User();
            newUser.setGoogleId(googleId);
            newUser.setEmail(email);
            newUser.setDisplayName(displayName);
            newUser.setUserType(User.UserType.USER); // Default to regular user
            newUser.setFirstLoginDate(LocalDateTime.now());
            newUser.setLastLoginDate(LocalDateTime.now());
            return userRepository.save(newUser);
        }
    }

    /**
     * Find a user by Google ID.
     *
     * @param googleId Google OAuth user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    /**
     * Find a user by email address.
     *
     * @param email User's email
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get a user by ID.
     *
     * @param userId User's ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Update a user's type (USER or ADMIN).
     * Only administrators should be able to call this method.
     *
     * @param userId   User's ID
     * @param userType New user type
     * @return Updated user
     * @throws IllegalArgumentException if user not found
     */
    public User updateUserType(Long userId, User.UserType userType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setUserType(userType);
        return userRepository.save(user);
    }

    /**
     * Check if a user is an administrator.
     *
     * @param userId User's ID
     * @return true if user is an admin, false otherwise
     */
    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUserType() == User.UserType.ADMIN)
                .orElse(false);
    }
}
