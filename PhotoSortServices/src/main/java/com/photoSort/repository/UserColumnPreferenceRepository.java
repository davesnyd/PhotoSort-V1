/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.User;
import com.photoSort.model.UserColumnPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserColumnPreference entity.
 * Provides database access methods for user column preferences.
 */
@Repository
public interface UserColumnPreferenceRepository extends JpaRepository<UserColumnPreference, Long> {

    /**
     * Find all column preferences for a specific user.
     *
     * @param user The user
     * @return List of column preferences ordered by display order
     */
    List<UserColumnPreference> findByUserOrderByDisplayOrderAsc(User user);

    /**
     * Delete all column preferences for a specific user.
     *
     * @param user The user
     */
    void deleteByUser(User user);
}
