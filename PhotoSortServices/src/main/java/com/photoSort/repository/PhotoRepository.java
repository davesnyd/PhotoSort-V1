/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.Photo;
import com.photoSort.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Photo entity.
 * Provides database access methods for photo management.
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    /**
     * Find a photo by its file path.
     *
     * @param filePath The file path
     * @return Optional containing the photo if found
     */
    Optional<Photo> findByFilePath(String filePath);

    /**
     * Find all photos owned by a specific user.
     *
     * @param owner The user who owns the photos
     * @return List of photos
     */
    List<Photo> findByOwner(User owner);

    /**
     * Count the number of photos owned by a specific user.
     *
     * @param owner The user who owns the photos
     * @return Count of photos
     */
    long countByOwner(User owner);

    /**
     * Find all public photos.
     *
     * @return List of public photos
     */
    List<Photo> findByIsPublicTrue();

    /**
     * Check if a photo exists with the given file path.
     *
     * @param filePath The file path
     * @return true if photo exists, false otherwise
     */
    boolean existsByFilePath(String filePath);
}
