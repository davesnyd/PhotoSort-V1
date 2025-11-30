/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Script entity.
 * Provides database access methods for script management.
 */
@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {

    /**
     * Find a script by its name.
     *
     * @param scriptName The script name
     * @return Optional containing the script if found
     */
    Optional<Script> findByScriptName(String scriptName);

    /**
     * Find all scripts that process a specific file extension.
     *
     * @param fileExtension The file extension
     * @return List of scripts
     */
    List<Script> findByFileExtension(String fileExtension);

    /**
     * Find all scripts with a daily run time configured.
     *
     * @return List of scripts with run times
     */
    List<Script> findByRunTimeIsNotNull();

    /**
     * Find all scripts with periodic execution configured.
     *
     * @return List of scripts with periodicity
     */
    List<Script> findByPeriodicityMinutesIsNotNull();
}
