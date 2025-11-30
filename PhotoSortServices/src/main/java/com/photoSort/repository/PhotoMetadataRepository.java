/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.Photo;
import com.photoSort.model.PhotoMetadata;
import com.photoSort.model.MetadataField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PhotoMetadata entity.
 * Provides database access methods for photo metadata values.
 */
@Repository
public interface PhotoMetadataRepository extends JpaRepository<PhotoMetadata, Long> {

    /**
     * Find all metadata for a specific photo.
     *
     * @param photo The photo
     * @return List of metadata entries
     */
    List<PhotoMetadata> findByPhoto(Photo photo);

    /**
     * Find specific metadata for a photo by field.
     *
     * @param photo The photo
     * @param field The metadata field
     * @return Optional containing the metadata if found
     */
    Optional<PhotoMetadata> findByPhotoAndField(Photo photo, MetadataField field);

    /**
     * Delete all metadata for a specific photo.
     *
     * @param photo The photo
     */
    void deleteByPhoto(Photo photo);
}
