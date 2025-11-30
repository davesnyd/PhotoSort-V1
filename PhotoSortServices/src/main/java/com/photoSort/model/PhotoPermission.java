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
 * Entity representing access permissions for private photos.
 * Grants specific users access to view private photos they don't own.
 */
@Entity
@Table(name = "photo_permissions", indexes = {
    @Index(name = "idx_photo_permissions_photo_id", columnList = "photo_id"),
    @Index(name = "idx_photo_permissions_user_id", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_photo_permissions_photo_user", columnNames = {"photo_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
