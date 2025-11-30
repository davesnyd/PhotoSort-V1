/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.Photo;
import com.photoSort.model.PhotoPermission;
import com.photoSort.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PhotoPermission entity.
 * Provides database access methods for photo access permissions.
 */
@Repository
public interface PhotoPermissionRepository extends JpaRepository<PhotoPermission, Long> {

    /**
     * Find all permissions for a specific photo.
     *
     * @param photo The photo
     * @return List of permissions
     */
    List<PhotoPermission> findByPhoto(Photo photo);

    /**
     * Find all photos a user has been granted access to.
     *
     * @param user The user
     * @return List of permissions
     */
    List<PhotoPermission> findByUser(User user);

    /**
     * Delete all permissions for a specific photo.
     *
     * @param photo The photo
     */
    void deleteByPhoto(Photo photo);

    /**
     * Check if a user has permission to access a photo.
     *
     * @param photo The photo
     * @param user The user
     * @return true if permission exists, false otherwise
     */
    boolean existsByPhotoAndUser(Photo photo, User user);
}
