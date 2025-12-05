/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.photoSort.model.Photo;
import com.photoSort.model.Script;
import com.photoSort.model.ScriptExecutionLog;
import com.photoSort.repository.PhotoRepository;
import com.photoSort.repository.ScriptExecutionLogRepository;
import com.photoSort.repository.ScriptRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing custom scripts based on file extensions (Step 19)
 * Handles file-extension-based scripts, daily scheduled scripts, and periodic scripts
 */
@Service
public class ScriptExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExecutionService.class);

    private static final long TIMEOUT_SECONDS = 60;

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private ScriptExecutionLogRepository scriptExecutionLogRepository;

    @Autowired
    private PhotoRepository photoRepository;

    // Map of file extension to script
    private final Map<String, Script> extensionToScriptMap = new ConcurrentHashMap<>();

    // Track last execution time for periodic scripts
    private final Map<Long, LocalDateTime> lastExecutionMap = new ConcurrentHashMap<>();

    /**
     * Load scripts from database on startup
     */
    @PostConstruct
    public void init() {
        loadScripts();
    }

    /**
     * Load scripts from database into in-memory map
     */
    private void loadScripts() {
        logger.info("Loading scripts from database");

        List<Script> scripts = scriptRepository.findAll();

        int extensionScripts = 0;
        int dailyScripts = 0;
        int periodicScripts = 0;

        for (Script script : scripts) {
            // Add to extension map if script has file extension
            if (script.getFileExtension() != null && !script.getFileExtension().trim().isEmpty()) {
                extensionToScriptMap.put(script.getFileExtension(), script);
                extensionScripts++;
            }

            // Count scheduled scripts
            if (script.getRunTime() != null) {
                dailyScripts++;
            }

            if (script.getPeriodicityMinutes() != null) {
                periodicScripts++;
            }
        }

        logger.info("Loaded {} extension-based scripts, {} daily scripts, {} periodic scripts",
            extensionScripts, dailyScripts, periodicScripts);
    }

    /**
     * Reload scripts from database (called after script updates)
     */
    public void reloadScripts() {
        logger.info("Reloading scripts from database");
        extensionToScriptMap.clear();
        loadScripts();
    }

    /**
     * Get script for file extension
     *
     * @param extension File extension (e.g., ".jpg", ".png")
     * @return Script or null if no script for extension
     */
    public Script getScriptForExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return null;
        }

        return extensionToScriptMap.get(extension);
    }

    /**
     * Execute script for a file
     *
     * @param script The script to execute
     * @param file The file to process
     * @param photo The photo record (may be null)
     * @return true if execution succeeded, false otherwise
     */
    public boolean executeScript(Script script, File file, Photo photo) {
        if (script == null) {
            logger.warn("Cannot execute null script");
            return false;
        }

        if (file == null || !file.exists()) {
            logger.warn("Cannot execute script for non-existent file: {}", file);
            logExecution(script, photo, false, "File does not exist");
            return false;
        }

        logger.info("Executing script '{}' for file: {}", script.getScriptName(), file.getName());

        File scriptFile = null;
        boolean success = false;
        String errorMessage = null;

        try {
            // Create temp script file if script has contents
            if (script.getScriptContents() != null && !script.getScriptContents().trim().isEmpty()) {
                scriptFile = createTempScriptFile(script.getScriptContents());
            } else if (script.getScriptFileName() != null) {
                scriptFile = new File(script.getScriptFileName());
                if (!scriptFile.exists()) {
                    errorMessage = "Script file not found: " + script.getScriptFileName();
                    logger.warn(errorMessage);
                    logExecution(script, photo, false, errorMessage);
                    return false;
                }
            } else {
                errorMessage = "Script has no contents or file name";
                logger.warn(errorMessage);
                logExecution(script, photo, false, errorMessage);
                return false;
            }

            // Build process
            ProcessBuilder processBuilder = new ProcessBuilder(
                "/bin/bash",
                scriptFile.getAbsolutePath(),
                file.getAbsolutePath()
            );

            // Set working directory to file's parent directory
            processBuilder.directory(file.getParentFile());

            // Redirect error stream to output stream
            processBuilder.redirectErrorStream(true);

            // Start process
            Process process = processBuilder.start();

            // Wait for process to complete (with timeout) - MUST DO THIS BEFORE READING OUTPUT
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                errorMessage = "Script timed out after " + TIMEOUT_SECONDS + " seconds";
                logger.warn("Script '{}' timed out for file: {}",
                    script.getScriptName(), file.getName());
                process.destroyForcibly();
                logExecution(script, photo, false, errorMessage);
                return false;
            }

            // Process completed - now read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                errorMessage = "Script exited with code " + exitCode + ": " + output.toString().trim();
                logger.warn("Script '{}' failed for file: {}. Exit code: {}",
                    script.getScriptName(), file.getName(), exitCode);
                logExecution(script, photo, false, errorMessage);
                return false;
            }

            // Success
            logger.info("Script '{}' executed successfully for file: {}",
                script.getScriptName(), file.getName());
            success = true;
            logExecution(script, photo, true, null);
            return true;

        } catch (Exception e) {
            errorMessage = "Exception executing script: " + e.getMessage();
            logger.error("Error executing script '{}' for file {}: {}",
                script.getScriptName(), file.getName(), e.getMessage(), e);
            logExecution(script, photo, false, errorMessage);
            return false;

        } finally {
            // Clean up temp script file
            if (scriptFile != null && script.getScriptContents() != null) {
                try {
                    Files.deleteIfExists(scriptFile.toPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete temp script file: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Execute daily scheduled scripts (runs every minute, checks if time matches)
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void executeDailyScripts() {
        List<Script> dailyScripts = scriptRepository.findByRunTimeIsNotNull();

        if (dailyScripts.isEmpty()) {
            return;
        }

        LocalTime currentTime = LocalTime.now();

        for (Script script : dailyScripts) {
            LocalTime runTime = script.getRunTime();

            // Check if current time matches run time (within 1 minute)
            if (isWithinOneMinute(currentTime, runTime)) {
                logger.info("Executing daily script: {}", script.getScriptName());
                executeScheduledScript(script);
            }
        }
    }

    /**
     * Execute periodic scripts (runs every minute, checks if period has elapsed)
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void executePeriodicScripts() {
        List<Script> periodicScripts = scriptRepository.findByPeriodicityMinutesIsNotNull();

        if (periodicScripts.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (Script script : periodicScripts) {
            Long scriptId = script.getScriptId();
            Integer periodicityMinutes = script.getPeriodicityMinutes();

            LocalDateTime lastExecution = lastExecutionMap.get(scriptId);

            // If never executed, or periodicity has elapsed, execute
            if (lastExecution == null ||
                now.isAfter(lastExecution.plusMinutes(periodicityMinutes))) {

                logger.info("Executing periodic script: {} (every {} minutes)",
                    script.getScriptName(), periodicityMinutes);
                executeScheduledScript(script);
                lastExecutionMap.put(scriptId, now);
            }
        }
    }

    /**
     * Execute a scheduled script (no specific file to process)
     */
    private void executeScheduledScript(Script script) {
        // For scheduled scripts, execute without a specific file
        // Script should handle its own logic
        try {
            File scriptFile = null;

            if (script.getScriptContents() != null && !script.getScriptContents().trim().isEmpty()) {
                scriptFile = createTempScriptFile(script.getScriptContents());
            } else if (script.getScriptFileName() != null) {
                scriptFile = new File(script.getScriptFileName());
                if (!scriptFile.exists()) {
                    logger.warn("Script file not found: {}", script.getScriptFileName());
                    logExecution(script, null, false, "Script file not found");
                    return;
                }
            } else {
                logger.warn("Script has no contents or file name");
                logExecution(script, null, false, "No script contents or file name");
                return;
            }

            // Build process
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", scriptFile.getAbsolutePath());

            // Redirect error stream to output stream
            processBuilder.redirectErrorStream(true);

            // Start process
            Process process = processBuilder.start();

            // Wait for process to complete (with timeout) - MUST DO THIS BEFORE READING OUTPUT
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                logger.warn("Scheduled script '{}' timed out", script.getScriptName());
                process.destroyForcibly();
                logExecution(script, null, false, "Timeout");
                return;
            }

            // Process completed - now read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.warn("Scheduled script '{}' failed with exit code: {}", script.getScriptName(), exitCode);
                logExecution(script, null, false, "Exit code " + exitCode);
            } else {
                logger.info("Scheduled script '{}' executed successfully", script.getScriptName());
                logExecution(script, null, true, null);
            }

            // Clean up temp file
            if (script.getScriptContents() != null) {
                try {
                    Files.deleteIfExists(scriptFile.toPath());
                } catch (Exception e) {
                    logger.warn("Failed to delete temp script file: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error executing scheduled script '{}': {}", script.getScriptName(), e.getMessage(), e);
            logExecution(script, null, false, "Exception: " + e.getMessage());
        }
    }

    /**
     * Create temporary script file from script contents
     */
    private File createTempScriptFile(String scriptContents) throws Exception {
        File tempFile = Files.createTempFile("photosort_script_", ".sh").toFile();
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(scriptContents);
        }

        // Make executable
        tempFile.setExecutable(true);

        return tempFile;
    }

    /**
     * Log script execution to database
     */
    private void logExecution(Script script, Photo photo, boolean success, String errorMessage) {
        try {
            ScriptExecutionLog log = new ScriptExecutionLog();
            log.setScript(script);
            log.setPhoto(photo);
            log.setStatus(success ? ScriptExecutionLog.ExecutionStatus.SUCCESS : ScriptExecutionLog.ExecutionStatus.FAILURE);
            log.setErrorMessage(errorMessage);

            scriptExecutionLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to log script execution: {}", e.getMessage());
        }
    }

    /**
     * Check if two times are within one minute of each other
     */
    private boolean isWithinOneMinute(LocalTime time1, LocalTime time2) {
        // Convert to seconds and compare
        long diff = Math.abs(time1.toSecondOfDay() - time2.toSecondOfDay());
        return diff < 60;
    }
}
