/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.MetadataField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for MetadataField entity.
 * Provides database access methods for metadata field definitions.
 */
@Repository
public interface MetadataFieldRepository extends JpaRepository<MetadataField, Long> {

    /**
     * Find a metadata field by its name.
     *
     * @param fieldName The field name
     * @return Optional containing the metadata field if found
     */
    Optional<MetadataField> findByFieldName(String fieldName);

    /**
     * Check if a metadata field exists with the given name.
     *
     * @param fieldName The field name
     * @return true if field exists, false otherwise
     */
    boolean existsByFieldName(String fieldName);
}
