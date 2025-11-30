/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a photo in the PhotoSort system.
 * Photos are owned by users and can have associated metadata, tags, and EXIF data.
 */
@Entity
@Table(name = "photos", indexes = {
    @Index(name = "idx_photos_owner_id", columnList = "owner_id"),
    @Index(name = "idx_photos_file_name", columnList = "file_name"),
    @Index(name = "idx_photos_added_to_system_date", columnList = "added_to_system_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_photos_file_path", columnNames = "file_path")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "file_path", nullable = false, unique = true, length = 1000)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_created_date")
    private LocalDateTime fileCreatedDate;

    @Column(name = "file_modified_date")
    private LocalDateTime fileModifiedDate;

    @Column(name = "added_to_system_date", insertable = false, updatable = false)
    private LocalDateTime addedToSystemDate;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "thumbnail_path", length = 1000)
    private String thumbnailPath;
}
