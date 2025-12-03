/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.model.MetadataField;
import com.photoSort.repository.MetadataFieldRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test cases for Step 8: Modify Columns Dialog
 * Tests the metadata fields endpoint functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MetadataFieldRepository metadataFieldRepository;

    @BeforeEach
    public void setUp() {
        // Clean up any existing metadata fields
        metadataFieldRepository.deleteAll();

        // Add some test metadata fields
        MetadataField field1 = new MetadataField();
        field1.setFieldName("Location");
        metadataFieldRepository.save(field1);

        MetadataField field2 = new MetadataField();
        field2.setFieldName("Event");
        metadataFieldRepository.save(field2);

        MetadataField field3 = new MetadataField();
        field3.setFieldName("People");
        metadataFieldRepository.save(field3);
    }

    /**
     * Test Case 1: Verify endpoint returns all available metadata field names
     */
    @Test
    public void testModifyColumnsDialog_GetMetadataFields() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/metadata/fields"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<List<String>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<String>>>() {});

        assertTrue(response.isSuccess(), "Response should be successful");
        assertNotNull(response.getData(), "Data should not be null");

        List<String> fieldNames = response.getData();
        assertTrue(fieldNames.size() >= 3, "Should have at least 3 metadata fields");
        assertTrue(fieldNames.contains("Location"), "Should contain Location field");
        assertTrue(fieldNames.contains("Event"), "Should contain Event field");
        assertTrue(fieldNames.contains("People"), "Should contain People field");
    }

    /**
     * Test Case 2: Verify endpoint includes standard photo columns
     */
    @Test
    public void testModifyColumnsDialog_IncludesStandardColumns() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/metadata/fields"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<List<String>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<String>>>() {});

        List<String> fieldNames = response.getData();

        // Standard columns that should always be available
        assertTrue(fieldNames.contains("file_name"), "Should include file_name");
        assertTrue(fieldNames.contains("thumbnail"), "Should include thumbnail");
        assertTrue(fieldNames.contains("file_created_date"), "Should include file_created_date");
    }

    /**
     * Test Case 3: Verify endpoint includes EXIF fields
     */
    @Test
    public void testModifyColumnsDialog_IncludesExifFields() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/metadata/fields"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<List<String>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<String>>>() {});

        List<String> fieldNames = response.getData();

        // EXIF fields that should be available
        assertTrue(fieldNames.contains("camera_make"), "Should include camera_make");
        assertTrue(fieldNames.contains("camera_model"), "Should include camera_model");
        assertTrue(fieldNames.contains("date_time_original"), "Should include date_time_original");
    }

    /**
     * Test Case 4: Verify new metadata fields appear in list
     */
    @Test
    public void testModifyColumnsDialog_NewMetadataFieldsAppear() throws Exception {
        // Add a new metadata field
        MetadataField newField = new MetadataField();
        newField.setFieldName("CustomField123");
        metadataFieldRepository.save(newField);

        MvcResult result = mockMvc.perform(get("/api/metadata/fields"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<List<String>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<String>>>() {});

        List<String> fieldNames = response.getData();
        assertTrue(fieldNames.contains("CustomField123"),
                "New metadata field should appear in list");
    }

    /**
     * Test Case 5: Verify endpoint returns empty custom fields list if none exist
     */
    @Test
    public void testModifyColumnsDialog_NoCustomFields() throws Exception {
        // Remove all custom metadata fields
        metadataFieldRepository.deleteAll();

        MvcResult result = mockMvc.perform(get("/api/metadata/fields"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<List<String>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<String>>>() {});

        List<String> fieldNames = response.getData();

        // Should still have standard and EXIF fields
        assertTrue(fieldNames.size() > 0, "Should still have standard and EXIF fields");
        assertTrue(fieldNames.contains("file_name"), "Should include standard fields");
    }
}
