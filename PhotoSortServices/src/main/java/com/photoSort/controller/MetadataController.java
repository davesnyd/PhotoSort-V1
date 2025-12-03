/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.model.MetadataField;
import com.photoSort.repository.MetadataFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for metadata-related endpoints.
 * Provides access to available metadata fields for column customization.
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    // Standard photo table columns
    private static final List<String> STANDARD_COLUMNS = Arrays.asList(
            "file_name",
            "thumbnail",
            "file_created_date",
            "file_modified_date",
            "owner",
            "tags"
    );

    // Common EXIF fields that can be displayed as columns
    private static final List<String> EXIF_FIELDS = Arrays.asList(
            "camera_make",
            "camera_model",
            "date_time_original",
            "gps_latitude",
            "gps_longitude",
            "exposure_time",
            "f_number",
            "iso_speed",
            "focal_length",
            "image_width",
            "image_height",
            "orientation"
    );

    @Autowired
    private MetadataFieldRepository metadataFieldRepository;

    /**
     * Get all available metadata field names for column customization.
     * Combines standard columns, EXIF fields, and custom metadata fields.
     *
     * @return List of all available field names
     */
    @GetMapping("/fields")
    public ResponseEntity<ApiResponse<List<String>>> getAllMetadataFields() {
        try {
            List<String> allFields = new ArrayList<>();

            // Add standard columns
            allFields.addAll(STANDARD_COLUMNS);

            // Add EXIF fields
            allFields.addAll(EXIF_FIELDS);

            // Add custom metadata fields from database
            List<String> customFields = metadataFieldRepository.findAll().stream()
                    .map(MetadataField::getFieldName)
                    .collect(Collectors.toList());
            allFields.addAll(customFields);

            return ResponseEntity.ok(ApiResponse.success(allFields));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                            "Error retrieving metadata fields: " + e.getMessage()));
        }
    }
}
