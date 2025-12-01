/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides database access methods for user management with pagination and search support.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

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

    /**
     * Find all users with their photo counts using optimized JOIN query.
     * Returns array where element[0] is User and element[1] is Long (photo count).
     *
     * @param pageable Pagination and sorting parameters
     * @return List of Object arrays [User, Long photoCount]
     */
    @Query("SELECT u, COUNT(p) FROM User u LEFT JOIN Photo p ON p.owner = u GROUP BY u.userId")
    List<Object[]> findAllWithPhotoCounts(Pageable pageable);

    /**
     * Count total users for pagination when using findAllWithPhotoCounts.
     *
     * @return Total number of users
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    /**
     * Quick search users by email or display name (case-insensitive).
     *
     * @param search Search term
     * @param pageable Pagination and sorting parameters
     * @return Page of users matching search criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    /**
     * Quick search users with photo counts.
     *
     * @param search Search term
     * @param pageable Pagination and sorting parameters
     * @return List of Object arrays [User, Long photoCount]
     */
    @Query("SELECT u, COUNT(p) FROM User u LEFT JOIN Photo p ON p.owner = u " +
           "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "GROUP BY u.userId")
    List<Object[]> searchUsersWithPhotoCounts(@Param("search") String search, Pageable pageable);

    /**
     * Count users matching quick search criteria.
     *
     * @param search Search term
     * @return Number of users matching search
     */
    @Query("SELECT COUNT(u) FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :search, '%'))")
    long countSearchResults(@Param("search") String search);
}
