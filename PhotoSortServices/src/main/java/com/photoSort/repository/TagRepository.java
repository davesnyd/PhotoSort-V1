/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Tag entity.
 * Provides database access methods for tag management.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find a tag by its value.
     *
     * @param tagValue The tag value
     * @return Optional containing the tag if found
     */
    Optional<Tag> findByTagValue(String tagValue);

    /**
     * Check if a tag exists with the given value.
     *
     * @param tagValue The tag value
     * @return true if tag exists, false otherwise
     */
    boolean existsByTagValue(String tagValue);
}
