/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.photoSort.dto.ConfigurationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Service for managing system configuration (Step 13)
 * Reads configuration from Spring Environment and application.properties
 * Provides methods to get and update configuration with password redaction
 *
 * Note: Configuration changes are persisted to application.properties file
 * and also stored in-memory to take effect immediately without restart
 */
@Service
public class ConfigService {

    private static final String REDACTED_PASSWORD = "********";

    @Value("${config.properties.file:src/main/resources/application.properties}")
    private String propertiesFile;

    // In-memory map to store configuration overrides (takes precedence over application.properties)
    private final Map<String, String> configOverrides = new HashMap<>();

    @Autowired
    private Environment environment;

    /**
     * Get property value, checking overrides first, then environment
     * Made public for use by other services (e.g., GitPollingService)
     *
     * @param key Property key
     * @param defaultValue Default value if not found
     * @return Property value
     */
    public String getProperty(String key, String defaultValue) {
        if (configOverrides.containsKey(key)) {
            return configOverrides.get(key);
        }
        return environment.getProperty(key, defaultValue);
    }

    /**
     * Get current system configuration with passwords redacted
     *
     * @return ConfigurationDTO with redacted passwords
     */
    public ConfigurationDTO getConfiguration() {
        ConfigurationDTO config = new ConfigurationDTO();

        // Database configuration
        ConfigurationDTO.DatabaseConfig database = new ConfigurationDTO.DatabaseConfig();
        database.setUri(getProperty("spring.datasource.url", ""));
        database.setUsername(getProperty("spring.datasource.username", ""));
        database.setPassword(REDACTED_PASSWORD); // Always redact password
        config.setDatabase(database);

        // Git configuration
        ConfigurationDTO.GitConfig git = new ConfigurationDTO.GitConfig();
        git.setRepoPath(getProperty("git.repo.path", ""));
        git.setUrl(getProperty("git.repo.url", ""));
        git.setUsername(getProperty("git.username", ""));
        git.setToken(REDACTED_PASSWORD); // Always redact token
        git.setPollIntervalMinutes(
                Integer.parseInt(getProperty("git.poll.interval.minutes", "5"))
        );
        config.setGit(git);

        // OAuth configuration
        ConfigurationDTO.OAuthConfig oauth = new ConfigurationDTO.OAuthConfig();
        oauth.setClientId(getProperty("spring.security.oauth2.client.registration.google.client-id", ""));
        oauth.setClientSecret(REDACTED_PASSWORD); // Always redact secret
        oauth.setRedirectUri(getProperty("spring.security.oauth2.client.registration.google.redirect-uri", ""));
        config.setOauth(oauth);

        // STAG configuration
        ConfigurationDTO.StagConfig stag = new ConfigurationDTO.StagConfig();
        stag.setScriptPath(getProperty("stag.script.path", "./stag-main/stag.py"));
        stag.setPythonExecutable(getProperty("stag.python.executable", "python3"));
        config.setStag(stag);

        return config;
    }

    /**
     * Update system configuration
     * Only updates password fields if value is not "********"
     * Stores changes in memory to take effect immediately and persists to file
     *
     * @param config Configuration data to update
     * @throws IllegalArgumentException if configuration is invalid
     * @throws RuntimeException if unable to save to properties file
     */
    public void updateConfiguration(ConfigurationDTO config) {
        // Validate configuration first
        validateConfiguration(config);

        // Update database configuration
        if (config.getDatabase() != null) {
            ConfigurationDTO.DatabaseConfig db = config.getDatabase();
            if (db.getUri() != null) {
                configOverrides.put("spring.datasource.url", db.getUri());
            }
            if (db.getUsername() != null) {
                configOverrides.put("spring.datasource.username", db.getUsername());
            }
            // Only update password if it's not the redacted placeholder
            if (db.getPassword() != null && !REDACTED_PASSWORD.equals(db.getPassword())) {
                configOverrides.put("spring.datasource.password", db.getPassword());
            }
        }

        // Update git configuration
        if (config.getGit() != null) {
            ConfigurationDTO.GitConfig git = config.getGit();
            if (git.getRepoPath() != null) {
                configOverrides.put("git.repo.path", git.getRepoPath());
            }
            if (git.getUrl() != null) {
                configOverrides.put("git.repo.url", git.getUrl());
            }
            if (git.getUsername() != null) {
                configOverrides.put("git.username", git.getUsername());
            }
            // Only update token if it's not the redacted placeholder
            if (git.getToken() != null && !REDACTED_PASSWORD.equals(git.getToken())) {
                configOverrides.put("git.token", git.getToken());
            }
            if (git.getPollIntervalMinutes() != null) {
                configOverrides.put("git.poll.interval.minutes", git.getPollIntervalMinutes().toString());
            }
        }

        // Update OAuth configuration
        if (config.getOauth() != null) {
            ConfigurationDTO.OAuthConfig oauth = config.getOauth();
            if (oauth.getClientId() != null) {
                configOverrides.put("spring.security.oauth2.client.registration.google.client-id", oauth.getClientId());
            }
            // Only update secret if it's not the redacted placeholder
            if (oauth.getClientSecret() != null && !REDACTED_PASSWORD.equals(oauth.getClientSecret())) {
                configOverrides.put("spring.security.oauth2.client.registration.google.client-secret", oauth.getClientSecret());
            }
            if (oauth.getRedirectUri() != null) {
                configOverrides.put("spring.security.oauth2.client.registration.google.redirect-uri", oauth.getRedirectUri());
            }
        }

        // Update STAG configuration
        if (config.getStag() != null) {
            ConfigurationDTO.StagConfig stag = config.getStag();
            if (stag.getScriptPath() != null) {
                configOverrides.put("stag.script.path", stag.getScriptPath());
            }
            if (stag.getPythonExecutable() != null) {
                configOverrides.put("stag.python.executable", stag.getPythonExecutable());
            }
        }

        // Persist changes to application.properties file
        saveToPropertiesFile();
    }

    /**
     * Save configuration overrides to application.properties file
     * Preserves all existing properties and only updates changed values
     *
     * @throws RuntimeException if unable to read or write properties file
     */
    private void saveToPropertiesFile() {
        try {
            // Load existing properties from file
            Properties properties = new Properties();
            try (FileInputStream input = new FileInputStream(propertiesFile)) {
                properties.load(input);
            }

            // Update properties with overrides
            for (Map.Entry<String, String> entry : configOverrides.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }

            // Write properties back to file with header comment
            try (FileOutputStream output = new FileOutputStream(propertiesFile)) {
                properties.store(output, "Updated by PhotoSort Configuration Management");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration to properties file: " + e.getMessage(), e);
        }
    }

    /**
     * Validate configuration before saving
     *
     * @param config Configuration to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    private void validateConfiguration(ConfigurationDTO config) {
        // Validate database URI format
        if (config.getDatabase() != null && config.getDatabase().getUri() != null) {
            String uri = config.getDatabase().getUri();
            if (!uri.startsWith("jdbc:")) {
                throw new IllegalArgumentException("Database URI must start with 'jdbc:'");
            }
        }

        // Validate git poll interval (must be positive)
        if (config.getGit() != null && config.getGit().getPollIntervalMinutes() != null) {
            Integer interval = config.getGit().getPollIntervalMinutes();
            if (interval <= 0) {
                throw new IllegalArgumentException("Git poll interval must be positive");
            }
        }

        // Additional validations can be added here as needed
    }
}
