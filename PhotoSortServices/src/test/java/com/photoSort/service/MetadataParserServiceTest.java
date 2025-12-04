/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Step 16: Metadata File Parsing
 * Tests the metadata file parsing functionality for extracting custom metadata
 */
@SpringBootTest
@ActiveProfiles("test")
public class MetadataParserServiceTest {

    @Autowired(required = false)
    private MetadataParserService metadataParserService;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        // Setup will be implemented when service is created
    }

    /**
     * Test Case 1: Verify parsing of well-formatted .metadata file
     */
    @Test
    public void testMetadataParser_ParsesWellFormattedFile() throws Exception {
        assertNotNull(metadataParserService, "MetadataParserService should be created");

        // Create test .metadata file
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=Family Vacation 2024\n");
            writer.write("Location=Grand Canyon, Arizona\n");
            writer.write("Event=Summer Trip\n");
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify results
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Family Vacation 2024", result.get("Title"));
        assertEquals("Grand Canyon, Arizona", result.get("Location"));
        assertEquals("Summer Trip", result.get("Event"));
    }

    /**
     * Test Case 2: Verify tags field parsed as comma-separated list
     */
    @Test
    public void testMetadataParser_ParsesTagsAsCommaSeparatedList() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with tags
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=Test Photo\n");
            writer.write("tags=vacation,family,nature,landscape\n");
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify tags parsed as list
        assertNotNull(result);
        assertTrue(result.containsKey("tags"));
        Object tagsValue = result.get("tags");
        assertTrue(tagsValue instanceof List, "Tags should be parsed as List");

        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) tagsValue;
        assertEquals(4, tags.size());
        assertEquals("vacation", tags.get(0));
        assertEquals("family", tags.get(1));
        assertEquals("nature", tags.get(2));
        assertEquals("landscape", tags.get(3));
    }

    /**
     * Test Case 3: Verify tags with spaces are trimmed correctly
     */
    @Test
    public void testMetadataParser_TrimsTagsWithSpaces() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with spaces in tags
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("tags=  vacation , family  ,  nature,landscape  \n");
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify tags are trimmed
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) result.get("tags");
        assertEquals(4, tags.size());
        assertEquals("vacation", tags.get(0));
        assertEquals("family", tags.get(1));
        assertEquals("nature", tags.get(2));
        assertEquals("landscape", tags.get(3));
    }

    /**
     * Test Case 4: Verify handling of missing .metadata file (return empty map)
     */
    @Test
    public void testMetadataParser_HandlesMissingFile() throws Exception {
        assertNotNull(metadataParserService);

        // Reference non-existent file
        File metadataFile = new File(tempDir.toFile(), "nonexistent.metadata");

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify empty map returned
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test Case 5: Verify handling of malformed lines (skip or log warning)
     */
    @Test
    public void testMetadataParser_HandlesMalformedLines() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with malformed lines
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=Valid Line\n");
            writer.write("NoEqualsSign\n");  // Malformed - no '='
            writer.write("=NoKey\n");  // Malformed - no key
            writer.write("Location=Valid Location\n");
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify only valid lines parsed
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Valid Line", result.get("Title"));
        assertEquals("Valid Location", result.get("Location"));
    }

    /**
     * Test Case 6: Verify handling of duplicate keys (last value wins)
     */
    @Test
    public void testMetadataParser_DuplicateKeysLastValueWins() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with duplicate keys
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=First Value\n");
            writer.write("Title=Second Value\n");
            writer.write("Title=Final Value\n");
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify last value wins
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Final Value", result.get("Title"));
    }

    /**
     * Test Case 7: Verify empty values handled correctly
     */
    @Test
    public void testMetadataParser_HandlesEmptyValues() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with empty values
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=\n");  // Empty value
            writer.write("Location=Valid Location\n");
            writer.write("Event=  \n");  // Whitespace only
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify empty values are handled (stored as empty strings or skipped)
        assertNotNull(result);
        // Empty values should be stored
        assertTrue(result.containsKey("Title"));
        assertEquals("", result.get("Title"));
        assertEquals("Valid Location", result.get("Location"));
    }

    /**
     * Test Case 8: Verify special characters in values handled correctly
     */
    @Test
    public void testMetadataParser_HandlesSpecialCharacters() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with special characters
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=Photo @ Location #1\n");
            writer.write("Description=This & That: Special! Characters?\n");
            writer.write("Path=C:\\Users\\Photos\\file.jpg\n");
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify special characters preserved
        assertNotNull(result);
        assertEquals("Photo @ Location #1", result.get("Title"));
        assertEquals("This & That: Special! Characters?", result.get("Description"));
        assertEquals("C:\\Users\\Photos\\file.jpg", result.get("Path"));
    }

    /**
     * Test Case 9: Verify Unicode/international characters supported
     */
    @Test
    public void testMetadataParser_HandlesUnicodeCharacters() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with Unicode characters
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=北京故宫\n");  // Chinese characters
            writer.write("Location=São Paulo, Brasil\n");  // Portuguese with accents
            writer.write("Description=Café Français ♥\n");  // French with symbols
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify Unicode characters preserved
        assertNotNull(result);
        assertEquals("北京故宫", result.get("Title"));
        assertEquals("São Paulo, Brasil", result.get("Location"));
        assertEquals("Café Français ♥", result.get("Description"));
    }

    /**
     * Test Case 10: Verify handling of equals sign in value
     */
    @Test
    public void testMetadataParser_HandlesEqualsSignInValue() throws Exception {
        assertNotNull(metadataParserService);

        // Create test .metadata file with equals sign in value
        File metadataFile = new File(tempDir.toFile(), "test.metadata");
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Title=2+2=4\n");
            writer.write("Formula=E=mc^2\n");
        }

        // Parse file
        Map<String, Object> result = metadataParserService.parseMetadataFile(metadataFile);

        // Verify equals sign in value handled correctly
        assertNotNull(result);
        assertEquals("2+2=4", result.get("Title"));
        assertEquals("E=mc^2", result.get("Formula"));
    }
}
