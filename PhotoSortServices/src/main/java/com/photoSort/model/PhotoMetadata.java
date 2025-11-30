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
 * Entity representing custom metadata values associated with a photo.
 * Each record links a photo to a metadata field and stores the value.
 */
@Entity
@Table(name = "photo_metadata", indexes = {
    @Index(name = "idx_photo_metadata_photo_id", columnList = "photo_id"),
    @Index(name = "idx_photo_metadata_field_id", columnList = "field_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_photo_metadata_photo_field", columnNames = {"photo_id", "field_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metadata_id")
    private Long metadataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private MetadataField field;

    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private String metadataValue;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
