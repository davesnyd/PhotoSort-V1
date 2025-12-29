/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.dto.ConfigurationDTO;
import com.photoSort.model.Photo;
import com.photoSort.repository.PhotoRepository;
import com.photoSort.service.ConfigService;
import com.photoSort.service.GitPollingService;
import com.photoSort.service.PhotoProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Controller for configuration management endpoints (Step 13: Configuration Management Page)
 * Provides endpoints to get and update system configuration (admin only)
 */
@RestController
@RequestMapping("/api/config")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private GitPollingService gitPollingService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoProcessingService photoProcessingService;

    /**
     * Get current system configuration with passwords redacted
     * Admin only endpoint
     *
     * @return Configuration data with passwords shown as "********"
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ConfigurationDTO>> getConfiguration() {
        try {
            ConfigurationDTO config = configService.getConfiguration();
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error retrieving configuration: " + e.getMessage()));
        }
    }

    /**
     * Update system configuration
     * Admin only endpoint
     * Only updates password fields if value is not "********"
     *
     * @param config Configuration data to update
     * @return Success response
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateConfiguration(@RequestBody ConfigurationDTO config) {
        try {
            configService.updateConfiguration(config);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_CONFIGURATION",
                          "Invalid configuration: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error updating configuration: " + e.getMessage()));
        }
    }

    /**
     * Trigger a full rescan of the photo repository
     * Admin only endpoint
     * Clears the poll state and processes all photos
     *
     * @return Number of photos processed or error
     */
    @PostMapping("/rescan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerRescan() {
        try {
            int processedCount = gitPollingService.triggerFullRescan();

            if (processedCount < 0) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("RESCAN_FAILED",
                              "Rescan failed. Check that git.repo.path is configured and the directory exists."));
            }

            Map<String, Object> result = Map.of(
                "photosProcessed", processedCount,
                "message", "Rescan completed successfully"
            );

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error during rescan: " + e.getMessage()));
        }
    }

    /**
     * Reprocess all existing photos
     * Admin only endpoint
     * Regenerates thumbnails, re-extracts EXIF, metadata, and tags for all photos
     *
     * @return Number of photos reprocessed
     */
    @PostMapping("/reprocess-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reprocessAllPhotos() {
        try {
            List<Photo> allPhotos = photoRepository.findAll();
            int successCount = 0;
            int errorCount = 0;

            logger.info("Starting reprocess of {} photos", allPhotos.size());

            for (Photo photo : allPhotos) {
                try {
                    String filePath = photo.getFilePath();
                    if (filePath == null || filePath.isEmpty()) {
                        logger.warn("Photo {} has no file path, skipping", photo.getPhotoId());
                        errorCount++;
                        continue;
                    }

                    File photoFile = new File(filePath);
                    if (!photoFile.exists()) {
                        logger.warn("Photo file not found: {}, skipping", filePath);
                        errorCount++;
                        continue;
                    }

                    String ownerEmail = photo.getOwner() != null ? photo.getOwner().getEmail() : null;
                    photoProcessingService.processPhoto(photoFile, ownerEmail);
                    successCount++;

                    if (successCount % 10 == 0) {
                        logger.info("Reprocessed {}/{} photos", successCount, allPhotos.size());
                    }
                } catch (Exception e) {
                    logger.error("Error reprocessing photo {}: {}", photo.getPhotoId(), e.getMessage());
                    errorCount++;
                }
            }

            logger.info("Reprocess complete: {} succeeded, {} failed", successCount, errorCount);

            Map<String, Object> result = Map.of(
                "photosReprocessed", successCount,
                "errors", errorCount,
                "total", allPhotos.size(),
                "message", "Reprocess completed"
            );

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error during reprocess: " + e.getMessage()));
        }
    }
}
