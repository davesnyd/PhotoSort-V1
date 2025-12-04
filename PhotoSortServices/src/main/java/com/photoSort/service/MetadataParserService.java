/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Service for parsing .metadata files accompanying photos (Step 16)
 * Metadata files contain key=value pairs with custom metadata
 */
@Service
public class MetadataParserService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataParserService.class);

    /**
     * Parse .metadata file and return map of field names to values
     *
     * @param metadataFile The .metadata file to parse
     * @return Map of field names to values (String for regular fields, List<String> for tags field)
     */
    public Map<String, Object> parseMetadataFile(File metadataFile) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        // Check if file exists
        if (metadataFile == null || !metadataFile.exists() || !metadataFile.isFile()) {
            logger.debug("Metadata file does not exist: {}", metadataFile);
            return metadata;
        }

        logger.debug("Parsing metadata file: {}", metadataFile.getName());

        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip empty lines
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Parse key=value
                int equalsIndex = line.indexOf('=');

                // Skip malformed lines (no equals sign)
                if (equalsIndex == -1) {
                    logger.warn("Malformed line {} in {}: no equals sign (skipping)", lineNumber, metadataFile.getName());
                    continue;
                }

                // Extract key and value
                String key = line.substring(0, equalsIndex).trim();
                String value = equalsIndex < line.length() - 1 ? line.substring(equalsIndex + 1) : "";

                // Skip lines with empty keys
                if (key.isEmpty()) {
                    logger.warn("Malformed line {} in {}: empty key (skipping)", lineNumber, metadataFile.getName());
                    continue;
                }

                // Special handling for "tags" field
                if ("tags".equals(key)) {
                    List<String> tags = parseTags(value);
                    metadata.put(key, tags);
                } else {
                    // Regular field - store as string
                    metadata.put(key, value);
                }
            }

            logger.debug("Parsed {} field(s) from {}", metadata.size(), metadataFile.getName());

        } catch (IOException e) {
            logger.error("Error reading metadata file {}: {}", metadataFile.getName(), e.getMessage(), e);
        }

        return metadata;
    }

    /**
     * Parse comma-separated tags and return list
     *
     * @param tagsValue Comma-separated tags string
     * @return List of trimmed tag values
     */
    private List<String> parseTags(String tagsValue) {
        List<String> tags = new ArrayList<>();

        if (tagsValue == null || tagsValue.trim().isEmpty()) {
            return tags;
        }

        // Split on comma and trim each tag
        String[] tagArray = tagsValue.split(",");
        for (String tag : tagArray) {
            String trimmedTag = tag.trim();
            if (!trimmedTag.isEmpty()) {
                tags.add(trimmedTag);
            }
        }

        return tags;
    }
}
