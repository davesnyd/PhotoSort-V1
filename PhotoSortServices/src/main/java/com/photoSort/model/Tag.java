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
 * Entity representing a tag that can be applied to photos.
 * Tags are reusable labels for categorizing photos.
 */
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tags_tag_value", columnList = "tag_value")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_tags_tag_value", columnNames = "tag_value")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_value", unique = true, nullable = false, length = 100)
    private String tagValue;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
