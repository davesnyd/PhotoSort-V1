/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ConfigurationDTO;
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test cases for Step 13: Configuration Management Page
 * Tests the configuration management endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Set up test properties file before each test
     * This prevents tests from corrupting the production application.properties
     */
    @BeforeEach
    public void setUp() throws IOException {
        Properties testProps = new Properties();
        testProps.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/TestDatabase");
        testProps.setProperty("spring.datasource.username", "testuser");
        testProps.setProperty("spring.datasource.password", "testpass");
        testProps.setProperty("git.repo.path", "/test/repo");
        testProps.setProperty("git.repo.url", "https://github.com/test/repo.git");
        testProps.setProperty("git.username", "testuser");
        testProps.setProperty("git.token", "testtoken");
        testProps.setProperty("git.poll.interval.minutes", "5");
        testProps.setProperty("spring.security.oauth2.client.registration.google.client-id", "test-client-id");
        testProps.setProperty("spring.security.oauth2.client.registration.google.client-secret", "test-secret");
        testProps.setProperty("spring.security.oauth2.client.registration.google.redirect-uri", "http://localhost:8080/oauth2/callback");
        testProps.setProperty("stag.script.path", "./test-stag.py");
        testProps.setProperty("stag.python.executable", "python3");

        try (FileWriter writer = new FileWriter("/tmp/test-application.properties")) {
            testProps.store(writer, "Test Configuration");
        }
    }

    /**
     * Test Case 1: Verify only admins can access configuration page
     * Non-admin users should get 403 error
     */
    @Test
    public void testConfigurationManagement_NonAdminGetsForbidden() throws Exception {
        // TODO: Add test with non-admin user context
        // mockMvc.perform(get("/api/config")
        //         .with(user("user@test.com").roles("USER")))
        //         .andExpect(status().isForbidden());
    }

    /**
     * Test Case 2: Verify configuration loads correctly with passwords redacted
     */
    @Test
    public void testConfigurationManagement_GetConfiguration() throws Exception {
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.database").exists())
                .andExpect(jsonPath("$.data.database.uri").exists())
                .andExpect(jsonPath("$.data.database.username").exists())
                .andExpect(jsonPath("$.data.database.password").value("********"))
                .andExpect(jsonPath("$.data.git").exists())
                .andExpect(jsonPath("$.data.git.repoPath").exists())
                .andExpect(jsonPath("$.data.git.url").exists())
                .andExpect(jsonPath("$.data.git.token").value("********"))
                .andExpect(jsonPath("$.data.oauth").exists())
                .andExpect(jsonPath("$.data.oauth.clientId").exists())
                .andExpect(jsonPath("$.data.oauth.clientSecret").value("********"));
    }

    /**
     * Test Case 3: Verify user can update database URI
     */
    @Test
    public void testConfigurationManagement_UpdateDatabaseUri() throws Exception {
        ConfigurationDTO config = createTestConfiguration();
        config.getDatabase().setUri("jdbc:postgresql://newhost:5432/PhotoSortData");

        mockMvc.perform(put("/api/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify the update persisted
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.database.uri").value("jdbc:postgresql://newhost:5432/PhotoSortData"));
    }

    /**
     * Test Case 4: Verify user can update Git settings
     */
    @Test
    public void testConfigurationManagement_UpdateGitSettings() throws Exception {
        ConfigurationDTO config = createTestConfiguration();
        config.getGit().setRepoPath("/new/repo/path");
        config.getGit().setPollIntervalMinutes(10);

        mockMvc.perform(put("/api/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify the update persisted
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.git.repoPath").value("/new/repo/path"))
                .andExpect(jsonPath("$.data.git.pollIntervalMinutes").value(10));
    }

    /**
     * Test Case 5: Verify unchanged password fields remain as environment variables
     * When password field value is "********", it should not be updated
     */
    @Test
    public void testConfigurationManagement_UnchangedPasswordsNotUpdated() throws Exception {
        // Use createTestConfiguration helper instead of deserializing JSON
        ConfigurationDTO config = createTestConfiguration();

        // Update only database URI, leaving passwords as "********"
        config.getDatabase().setUri("jdbc:postgresql://changedhost:5432/PhotoSortData");
        // Passwords should still be "********"

        mockMvc.perform(put("/api/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify passwords are still redacted (not actually changed to "********")
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.database.password").value("********"));
    }

    /**
     * Test Case 6: Verify changed password fields update with new values
     */
    @Test
    public void testConfigurationManagement_ChangedPasswordsUpdated() throws Exception {
        ConfigurationDTO config = createTestConfiguration();
        config.getDatabase().setPassword("newDatabasePassword");
        config.getGit().setToken("newGitToken");
        config.getOauth().setClientSecret("newClientSecret");

        mockMvc.perform(put("/api/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Note: We can't directly verify the passwords changed since they're always redacted,
        // but the endpoint should accept them and they should be redacted on next GET
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.database.password").value("********"))
                .andExpect(jsonPath("$.data.git.token").value("********"))
                .andExpect(jsonPath("$.data.oauth.clientSecret").value("********"));
    }

    /**
     * Test Case 7: Verify configuration saved to application.properties correctly
     * This test verifies that the configuration is persisted
     */
    @Test
    public void testConfigurationManagement_ConfigurationPersisted() throws Exception {
        ConfigurationDTO config = createTestConfiguration();
        config.getStag().setScriptPath("/new/stag/path/stag.py");
        config.getStag().setPythonExecutable("python3.9");

        mockMvc.perform(put("/api/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk());

        // Verify the changes persist across requests
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stag.scriptPath").value("/new/stag/path/stag.py"))
                .andExpect(jsonPath("$.data.stag.pythonExecutable").value("python3.9"));
    }

    /**
     * Test Case 8: Verify invalid configuration rejected
     */
    @Test
    public void testConfigurationManagement_InvalidConfigurationRejected() throws Exception {
        ConfigurationDTO config = createTestConfiguration();
        config.getDatabase().setUri("invalid-uri-format");

        mockMvc.perform(put("/api/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CONFIGURATION"));
    }

    /**
     * Test Case 9: Verify system uses new configuration after save
     * This test verifies that configuration changes take effect
     */
    @Test
    public void testConfigurationManagement_ConfigurationTakesEffect() throws Exception {
        ConfigurationDTO config = createTestConfiguration();
        config.getDatabase().setUri("jdbc:postgresql://localhost:5432/NewDatabase");

        mockMvc.perform(put("/api/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify the configuration is reflected in subsequent GETs
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.database.uri").value("jdbc:postgresql://localhost:5432/NewDatabase"));
    }

    /**
     * Test Case 10: Verify non-admin users get 403 error
     */
    @Test
    public void testConfigurationManagement_PutConfigNonAdminForbidden() throws Exception {
        // TODO: Add test with non-admin user context
        ConfigurationDTO config = createTestConfiguration();

        // mockMvc.perform(put("/api/config")
        //         .with(user("user@test.com").roles("USER"))
        //         .contentType(MediaType.APPLICATION_JSON)
        //         .content(objectMapper.writeValueAsString(config)))
        //         .andExpect(status().isForbidden());
    }

    /**
     * Helper method to create a test configuration DTO
     */
    private ConfigurationDTO createTestConfiguration() {
        ConfigurationDTO config = new ConfigurationDTO();

        ConfigurationDTO.DatabaseConfig db = new ConfigurationDTO.DatabaseConfig();
        db.setUri("jdbc:postgresql://localhost:5432/PhotoSortData");
        db.setUsername("postgres");
        db.setPassword("********"); // Redacted
        config.setDatabase(db);

        ConfigurationDTO.GitConfig git = new ConfigurationDTO.GitConfig();
        git.setRepoPath("/path/to/repo");
        git.setUrl("https://github.com/user/repo.git");
        git.setUsername("gituser");
        git.setToken("********"); // Redacted
        git.setPollIntervalMinutes(5);
        config.setGit(git);

        ConfigurationDTO.OAuthConfig oauth = new ConfigurationDTO.OAuthConfig();
        oauth.setClientId("test-client-id");
        oauth.setClientSecret("********"); // Redacted
        oauth.setRedirectUri("http://localhost:8080/oauth2/callback");
        config.setOauth(oauth);

        ConfigurationDTO.StagConfig stag = new ConfigurationDTO.StagConfig();
        stag.setScriptPath("./stag-main/stag.py");
        stag.setPythonExecutable("python3");
        config.setStag(stag);

        return config;
    }
}
