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
 * Entity representing a user in the PhotoSort system.
 * Users can be either regular users or administrators.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_google_id", columnList = "google_id"),
    @Index(name = "idx_users_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "google_id", unique = true, nullable = false, length = 255)
    private String googleId;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "user_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "first_login_date", nullable = false)
    private LocalDateTime firstLoginDate;

    @Column(name = "last_login_date", nullable = false)
    private LocalDateTime lastLoginDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enum representing the types of users in the system.
     */
    public enum UserType {
        USER,
        ADMIN
    }
}
