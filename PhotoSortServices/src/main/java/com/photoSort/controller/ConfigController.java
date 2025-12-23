/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.dto.ConfigurationDTO;
import com.photoSort.service.ConfigService;
import com.photoSort.service.GitPollingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for configuration management endpoints (Step 13: Configuration Management Page)
 * Provides endpoints to get and update system configuration (admin only)
 */
@RestController
@RequestMapping("/api/config")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @Autowired
    private GitPollingService gitPollingService;

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
}
