/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing Git polling state (Step 14)
 * Tracks the last processed commit hash and poll time for Git repository monitoring
 */
@Entity
@Table(name = "git_poll_state")
public class GitPollState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_state_id")
    private Long pollStateId;

    @Column(name = "repository_path", unique = true, nullable = false, length = 1000)
    private String repositoryPath;

    @Column(name = "last_commit_hash", length = 40)
    private String lastCommitHash;

    @Column(name = "last_poll_time")
    private LocalDateTime lastPollTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public GitPollState() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public GitPollState(String repositoryPath) {
        this();
        this.repositoryPath = repositoryPath;
    }

    // Getters and Setters
    public Long getPollStateId() {
        return pollStateId;
    }

    public void setPollStateId(Long pollStateId) {
        this.pollStateId = pollStateId;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getLastCommitHash() {
        return lastCommitHash;
    }

    public void setLastCommitHash(String lastCommitHash) {
        this.lastCommitHash = lastCommitHash;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastPollTime() {
        return lastPollTime;
    }

    public void setLastPollTime(LocalDateTime lastPollTime) {
        this.lastPollTime = lastPollTime;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
