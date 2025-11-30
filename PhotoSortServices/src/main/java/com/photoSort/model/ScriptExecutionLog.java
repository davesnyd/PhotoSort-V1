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
 * Entity representing a log entry for script execution.
 * Tracks when scripts run, their status, and any errors encountered.
 */
@Entity
@Table(name = "script_execution_log", indexes = {
    @Index(name = "idx_script_execution_log_script_id", columnList = "script_id"),
    @Index(name = "idx_script_execution_log_execution_time", columnList = "execution_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id")
    private Script script;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @Column(name = "execution_time", insertable = false, updatable = false)
    private LocalDateTime executionTime;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Enum representing the execution status of a script.
     */
    public enum ExecutionStatus {
        SUCCESS,
        FAILURE
    }
}
