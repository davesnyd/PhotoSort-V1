/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for complete photo details including EXIF, metadata, and tags
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDetailDTO {
    private Long photoId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime fileCreatedDate;
    private LocalDateTime fileModifiedDate;
    private LocalDateTime addedToSystemDate;
    private Boolean isPublic;
    private Integer imageWidth;
    private Integer imageHeight;
    private String thumbnailPath;

    // Owner information
    private Long ownerId;
    private String ownerEmail;
    private String ownerDisplayName;

    // Related data
    private ExifDataDTO exifData;
    private List<MetadataDTO> metadata;
    private List<TagDTO> tags;
}
