/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for system configuration management (Step 13)
 * Contains all configuration sections: Database, Git, OAuth, and STAG
 * Password fields are redacted to "********" when retrieved
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationDTO {

    private DatabaseConfig database;
    private GitConfig git;
    private OAuthConfig oauth;
    private StagConfig stag;

    /**
     * Database configuration section
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseConfig {
        private String uri;
        private String username;
        private String password; // Redacted to "********" when retrieved
    }

    /**
     * Git repository configuration section
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GitConfig {
        private String repoPath;
        private String url;
        private String username;
        private String token; // Redacted to "********" when retrieved
        private Integer pollIntervalMinutes;
    }

    /**
     * OAuth configuration section
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OAuthConfig {
        private String clientId;
        private String clientSecret; // Redacted to "********" when retrieved
        private String redirectUri;
    }

    /**
     * STAG (AI tagging) configuration section
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StagConfig {
        private String scriptPath;
        private String pythonExecutable;
    }
}
