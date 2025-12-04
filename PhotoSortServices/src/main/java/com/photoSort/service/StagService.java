/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing STAG Python script for AI-generated photo tagging (Step 17)
 * STAG (Simple Tag Auto-Generator) analyzes photos and generates descriptive tags
 */
@Service
public class StagService {

    private static final Logger logger = LoggerFactory.getLogger(StagService.class);

    private static final long TIMEOUT_SECONDS = 30;

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigService configService;

    /**
     * Generate AI tags for a photo using STAG script
     *
     * @param photoFile The photo file to analyze
     * @return List of generated tag strings (empty list if execution fails)
     */
    public List<String> generateTags(File photoFile) {
        List<String> tags = new ArrayList<>();

        // Validate input
        if (photoFile == null || !photoFile.exists() || !photoFile.isFile()) {
            logger.warn("Invalid photo file for STAG processing: {}", photoFile);
            return tags;
        }

        // Get configuration
        String pythonExecutable = configService.getProperty("stag.python.executable", "python3");
        String scriptPath = configService.getProperty("stag.script.path", "./stag-main/stag.py");

        // Check if script exists
        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            logger.warn("STAG script not found at: {} (tags will be empty)", scriptPath);
            return tags;
        }

        logger.debug("Executing STAG script for: {}", photoFile.getName());

        try {
            // Build process
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonExecutable,
                scriptPath,
                photoFile.getAbsolutePath()
            );

            // Set working directory to script directory
            processBuilder.directory(scriptFile.getParentFile());

            // Redirect error stream to output stream
            processBuilder.redirectErrorStream(true);

            // Start process
            Process process = processBuilder.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for process to complete (with timeout)
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                logger.warn("STAG script timed out after {} seconds for: {}",
                    TIMEOUT_SECONDS, photoFile.getName());
                process.destroyForcibly();
                return tags;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.warn("STAG script exited with code {} for: {}\nOutput: {}",
                    exitCode, photoFile.getName(), output.toString().trim());
                return tags;
            }

            // Parse output
            tags = parseTags(output.toString());
            logger.debug("STAG generated {} tag(s) for: {}", tags.size(), photoFile.getName());

        } catch (Exception e) {
            logger.error("Error executing STAG script for {}: {}", photoFile.getName(), e.getMessage(), e);
        }

        return tags;
    }

    /**
     * Parse tags from STAG script output
     * Supports both comma-separated and newline-separated formats
     *
     * @param output The script output
     * @return List of parsed tags
     */
    private List<String> parseTags(String output) {
        List<String> tags = new ArrayList<>();

        if (output == null || output.trim().isEmpty()) {
            return tags;
        }

        String trimmedOutput = output.trim();

        // Try comma-separated first
        if (trimmedOutput.contains(",")) {
            String[] parts = trimmedOutput.split(",");
            for (String part : parts) {
                String tag = part.trim();
                if (!tag.isEmpty() && !tag.contains("\n")) {
                    tags.add(tag);
                }
            }
        }

        // If no comma-separated tags found, try newline-separated
        if (tags.isEmpty()) {
            String[] lines = trimmedOutput.split("\n");
            for (String line : lines) {
                String tag = line.trim();
                if (!tag.isEmpty()) {
                    tags.add(tag);
                }
            }
        }

        return tags;
    }
}
