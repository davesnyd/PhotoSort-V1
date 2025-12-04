/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.GitPollState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for GitPollState entity (Step 14)
 * Manages Git polling state in the database
 */
@Repository
public interface GitPollStateRepository extends JpaRepository<GitPollState, Long> {

    /**
     * Find poll state by repository path
     * @param repositoryPath The path to the Git repository
     * @return Optional containing GitPollState if found
     */
    Optional<GitPollState> findByRepositoryPath(String repositoryPath);

    /**
     * Check if poll state exists for repository path
     * @param repositoryPath The path to the Git repository
     * @return true if exists, false otherwise
     */
    boolean existsByRepositoryPath(String repositoryPath);
}
