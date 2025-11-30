/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides database access methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their Google ID.
     *
     * @param googleId The Google OAuth ID
     * @return Optional containing the user if found
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Find a user by their email address.
     *
     * @param email The user's email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given Google ID.
     *
     * @param googleId The Google OAuth ID
     * @return true if user exists, false otherwise
     */
    boolean existsByGoogleId(String googleId);
}
