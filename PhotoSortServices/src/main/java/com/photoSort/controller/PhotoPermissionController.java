/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.model.Photo;
import com.photoSort.model.PhotoPermission;
import com.photoSort.model.User;
import com.photoSort.repository.PhotoPermissionRepository;
import com.photoSort.repository.PhotoRepository;
import com.photoSort.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing photo access permissions.
 * Allows photo owners to grant/revoke access to their private photos.
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoPermissionController {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoPermissionRepository photoPermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get list of user IDs who have access to a photo.
     *
     * @param photoId Photo ID
     * @return List of user IDs with access
     */
    @GetMapping("/{photoId}/permissions")
    public ResponseEntity<ApiResponse<List<Long>>> getPhotoPermissions(@PathVariable Long photoId) {
        try {
            // Verify photo exists
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("PHOTO_NOT_FOUND", "Photo not found"));
            }

            // Get all permissions for this photo
            List<Long> userIds = photoPermissionRepository.findByPhoto(photo).stream()
                    .map(p -> p.getUser().getUserId())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(userIds));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                            "Error retrieving permissions: " + e.getMessage()));
        }
    }

    /**
     * Update photo permissions.
     * Replaces all existing permissions with the provided list.
     *
     * @param photoId Photo ID
     * @param userIds List of user IDs to grant access
     * @return Success message
     */
    @PutMapping("/{photoId}/permissions")
    @Transactional
    public ResponseEntity<ApiResponse<String>> updatePhotoPermissions(
            @PathVariable Long photoId,
            @RequestBody List<Long> userIds) {

        try {
            // Verify photo exists
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("PHOTO_NOT_FOUND", "Photo not found"));
            }

            // Remove all existing permissions for this photo
            List<PhotoPermission> existingPermissions = photoPermissionRepository.findByPhoto(photo);
            photoPermissionRepository.deleteAll(existingPermissions);

            // Flush to ensure deletes are committed before inserts
            entityManager.flush();

            // Create new permissions
            for (Long userId : userIds) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    PhotoPermission permission = new PhotoPermission();
                    permission.setPhoto(photo);
                    permission.setUser(user);
                    photoPermissionRepository.save(permission);
                }
            }

            return ResponseEntity.ok(ApiResponse.success("Permissions updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                            "Error updating permissions: " + e.getMessage()));
        }
    }
}
