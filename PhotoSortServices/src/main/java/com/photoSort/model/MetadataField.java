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
 * Entity representing a custom metadata field name.
 * This table stores the possible field names that can be associated with photos.
 */
@Entity
@Table(name = "metadata_fields", indexes = {
    @Index(name = "idx_metadata_fields_field_name", columnList = "field_name")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_metadata_fields_field_name", columnNames = "field_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "field_name", unique = true, nullable = false, length = 100)
    private String fieldName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
