/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.Photo;
import com.photoSort.model.PhotoTag;
import com.photoSort.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PhotoTag entity.
 * Provides database access methods for photo-tag associations.
 */
@Repository
public interface PhotoTagRepository extends JpaRepository<PhotoTag, Long> {

    /**
     * Find all tags associated with a specific photo.
     *
     * @param photo The photo
     * @return List of photo-tag associations
     */
    List<PhotoTag> findByPhoto(Photo photo);

    /**
     * Find all photos associated with a specific tag.
     *
     * @param tag The tag
     * @return List of photo-tag associations
     */
    List<PhotoTag> findByTag(Tag tag);

    /**
     * Delete all tag associations for a specific photo.
     *
     * @param photo The photo
     */
    void deleteByPhoto(Photo photo);

    /**
     * Delete all photo associations for a specific tag.
     *
     * @param tag The tag
     */
    void deleteByTag(Tag tag);
}
