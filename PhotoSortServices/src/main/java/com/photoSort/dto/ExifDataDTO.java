/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for EXIF data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExifDataDTO {
    private Long exifId;
    private LocalDateTime dateTimeOriginal;
    private String cameraMake;
    private String cameraModel;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;
    private String exposureTime;

    @JsonProperty("fNumber")
    private String fNumber;

    private Integer isoSpeed;
    private String focalLength;
    private Integer orientation;
}
