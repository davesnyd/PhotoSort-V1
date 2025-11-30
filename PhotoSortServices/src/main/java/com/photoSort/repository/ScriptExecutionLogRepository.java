/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.Photo;
import com.photoSort.model.Script;
import com.photoSort.model.ScriptExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ScriptExecutionLog entity.
 * Provides database access methods for script execution logging.
 */
@Repository
public interface ScriptExecutionLogRepository extends JpaRepository<ScriptExecutionLog, Long> {

    /**
     * Find all execution logs for a specific script.
     *
     * @param script The script
     * @return List of execution logs
     */
    List<ScriptExecutionLog> findByScript(Script script);

    /**
     * Find all execution logs for a specific photo.
     *
     * @param photo The photo
     * @return List of execution logs
     */
    List<ScriptExecutionLog> findByPhoto(Photo photo);

    /**
     * Find all execution logs for a script with a specific status.
     *
     * @param script The script
     * @param status The execution status
     * @return List of execution logs
     */
    List<ScriptExecutionLog> findByScriptAndStatus(Script script, ScriptExecutionLog.ExecutionStatus status);
}
