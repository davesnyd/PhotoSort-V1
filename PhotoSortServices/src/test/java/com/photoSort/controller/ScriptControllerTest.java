/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.model.Script;
import com.photoSort.repository.ScriptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test cases for Step 11: Scripts Table Page
 * Tests the script management endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ScriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScriptRepository scriptRepository;

    private Script script1;
    private Script script2;

    @BeforeEach
    public void setUp() {
        // Clean up
        scriptRepository.deleteAll();

        // Create test scripts
        script1 = new Script();
        script1.setScriptName("Photo Resize Script");
        script1.setScriptFileName("resize_photos.py");
        script1.setScriptContents("#!/usr/bin/env python3\nprint('Resizing photos')");
        script1.setRunTime(LocalTime.of(2, 0)); // 2:00 AM
        script1.setFileExtension(".jpg");
        script1 = scriptRepository.save(script1);

        script2 = new Script();
        script2.setScriptName("Metadata Extractor");
        script2.setScriptFileName("extract_metadata.sh");
        script2.setScriptContents("#!/bin/bash\necho 'Extracting metadata'");
        script2.setPeriodicityMinutes(60); // Every hour
        script2.setFileExtension(".png");
        script2 = scriptRepository.save(script2);
    }

    /**
     * Test Case 1: Get all scripts returns list
     */
    @Test
    public void testScriptsTablePage_GetAllScripts() throws Exception {
        mockMvc.perform(get("/api/scripts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].scriptName", containsInAnyOrder("Photo Resize Script", "Metadata Extractor")));
    }

    /**
     * Test Case 2: Get script by ID returns correct script
     */
    @Test
    public void testScriptsTablePage_GetScriptById() throws Exception {
        mockMvc.perform(get("/api/scripts/" + script1.getScriptId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scriptName").value("Photo Resize Script"))
                .andExpect(jsonPath("$.data.scriptFileName").value("resize_photos.py"))
                .andExpect(jsonPath("$.data.fileExtension").value(".jpg"));
    }

    /**
     * Test Case 3: Get non-existent script returns 404
     */
    @Test
    public void testScriptsTablePage_GetScriptById_NotFound() throws Exception {
        mockMvc.perform(get("/api/scripts/99999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 4: Create new script
     */
    @Test
    public void testScriptsTablePage_CreateScript() throws Exception {
        Script newScript = new Script();
        newScript.setScriptName("New Test Script");
        newScript.setScriptFileName("test.py");
        newScript.setScriptContents("print('test')");
        newScript.setRunTime(LocalTime.of(3, 30));
        newScript.setFileExtension(".gif");

        mockMvc.perform(post("/api/scripts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newScript)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scriptName").value("New Test Script"));

        // Verify it was saved
        mockMvc.perform(get("/api/scripts"))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    /**
     * Test Case 5: Update existing script
     */
    @Test
    public void testScriptsTablePage_UpdateScript() throws Exception {
        script1.setScriptName("Updated Script Name");
        script1.setFileExtension(".jpeg");

        mockMvc.perform(put("/api/scripts/" + script1.getScriptId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(script1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scriptName").value("Updated Script Name"))
                .andExpect(jsonPath("$.data.fileExtension").value(".jpeg"));
    }

    /**
     * Test Case 6: Update non-existent script returns 404
     */
    @Test
    public void testScriptsTablePage_UpdateScript_NotFound() throws Exception {
        Script nonExistent = new Script();
        nonExistent.setScriptId(99999L);
        nonExistent.setScriptName("Non-existent");

        mockMvc.perform(put("/api/scripts/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistent)))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 7: Delete script
     */
    @Test
    public void testScriptsTablePage_DeleteScript() throws Exception {
        mockMvc.perform(delete("/api/scripts/" + script1.getScriptId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify it was deleted
        mockMvc.perform(get("/api/scripts"))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    /**
     * Test Case 8: Delete non-existent script returns 404
     */
    @Test
    public void testScriptsTablePage_DeleteScript_NotFound() throws Exception {
        mockMvc.perform(delete("/api/scripts/99999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 9: Scripts with runTime are returned
     */
    @Test
    public void testScriptsTablePage_ScriptWithRunTime() throws Exception {
        mockMvc.perform(get("/api/scripts/" + script1.getScriptId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runTime").value("02:00:00"))
                .andExpect(jsonPath("$.data.periodicityMinutes").isEmpty());
    }

    /**
     * Test Case 10: Scripts with periodicity are returned
     */
    @Test
    public void testScriptsTablePage_ScriptWithPeriodicity() throws Exception {
        mockMvc.perform(get("/api/scripts/" + script2.getScriptId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.periodicityMinutes").value(60))
                .andExpect(jsonPath("$.data.runTime").isEmpty());
    }
}
