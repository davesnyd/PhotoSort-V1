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
 * Entity representing the association between a photo and a tag.
 * This is a junction table for the many-to-many relationship.
 */
@Entity
@Table(name = "photo_tags", indexes = {
    @Index(name = "idx_photo_tags_photo_id", columnList = "photo_id"),
    @Index(name = "idx_photo_tags_tag_id", columnList = "tag_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_photo_tags_photo_tag", columnNames = {"photo_id", "tag_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_tag_id")
    private Long photoTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
