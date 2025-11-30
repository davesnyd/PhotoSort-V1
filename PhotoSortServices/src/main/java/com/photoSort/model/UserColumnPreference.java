/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user's column display preferences for the photo table.
 * Allows users to customize which columns they see in their photo list view.
 */
@Entity
@Table(name = "user_column_preferences", indexes = {
    @Index(name = "idx_user_column_preferences_user_id", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_column_prefs", columnNames = {"user_id", "column_type", "column_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserColumnPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "column_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ColumnType columnType;

    @Column(name = "column_name", length = 100)
    private String columnName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * Enum representing the types of columns available.
     */
    public enum ColumnType {
        STANDARD,
        METADATA
    }
}
