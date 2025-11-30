/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.ExifData;
import com.photoSort.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ExifData entity.
 * Provides database access methods for EXIF metadata.
 */
@Repository
public interface ExifDataRepository extends JpaRepository<ExifData, Long> {

    /**
     * Find EXIF data for a specific photo.
     *
     * @param photo The photo
     * @return Optional containing the EXIF data if found
     */
    Optional<ExifData> findByPhoto(Photo photo);

    /**
     * Delete EXIF data for a specific photo.
     *
     * @param photo The photo
     */
    void deleteByPhoto(Photo photo);
}
