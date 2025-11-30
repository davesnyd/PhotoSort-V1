/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for PhotoSort Services.
 * This is a Spring Boot application that provides backend services
 * for photo management, including metadata extraction, tagging,
 * and user management.
 */
@SpringBootApplication
@EnableScheduling
public class PhotoSortApplication {

    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PhotoSortApplication.class, args);
    }
}
