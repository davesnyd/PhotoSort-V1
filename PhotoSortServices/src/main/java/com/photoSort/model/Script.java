/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing an automated script that processes photos.
 * Scripts can be scheduled to run daily or periodically.
 */
@Entity
@Table(name = "scripts", indexes = {
    @Index(name = "idx_scripts_file_extension", columnList = "file_extension")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_scripts_script_name", columnNames = "script_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Script {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "script_id")
    private Long scriptId;

    @Column(name = "script_name", unique = true, nullable = false, length = 100)
    private String scriptName;

    @Column(name = "script_file_name", length = 255)
    private String scriptFileName;

    @Column(name = "script_contents", columnDefinition = "TEXT")
    private String scriptContents;

    @Column(name = "run_time")
    private LocalTime runTime;

    @Column(name = "periodicity_minutes")
    private Integer periodicityMinutes;

    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
