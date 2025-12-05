/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for generating thumbnails for photo files (Step 20)
 * Creates 200x200px thumbnails with aspect ratio maintained
 */
@Service
public class ThumbnailService {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);

    private static final int MAX_THUMBNAIL_WIDTH = 200;
    private static final int MAX_THUMBNAIL_HEIGHT = 200;
    private static final double JPEG_QUALITY = 0.85;

    @Autowired
    private ConfigService configService;

    /**
     * Generate thumbnail for a photo file
     *
     * @param photoFile The photo file to generate thumbnail for
     * @return Absolute path to generated thumbnail, or null if generation failed
     */
    public String generateThumbnail(File photoFile) {
        if (photoFile == null || !photoFile.exists() || !photoFile.isFile()) {
            logger.warn("Cannot generate thumbnail: file does not exist or is not a file: {}", photoFile);
            return null;
        }

        try {
            // Get thumbnail directory from configuration
            String repoPath = configService.getProperty("git.repo.path", "/path/to/repo");
            if (repoPath.equals("/path/to/repo")) {
                logger.warn("Git repository path not configured, using temp directory for thumbnails");
                repoPath = System.getProperty("java.io.tmpdir");
            }

            Path thumbnailDir = Paths.get(repoPath, "thumbnails");

            // Create thumbnails directory if it doesn't exist
            if (!Files.exists(thumbnailDir)) {
                try {
                    Files.createDirectories(thumbnailDir);
                    logger.info("Created thumbnails directory: {}", thumbnailDir);
                } catch (IOException e) {
                    logger.error("Failed to create thumbnails directory: {}", e.getMessage());
                    return null;
                }
            }

            // Extract filename and extension
            String originalFilename = photoFile.getName();
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex); // includes the dot
            }

            // Generate thumbnail filename: original_name_thumb.ext
            String baseFilename = dotIndex > 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
            String thumbnailFilename = baseFilename + "_thumb" + extension;
            File thumbnailFile = new File(thumbnailDir.toFile(), thumbnailFilename);

            // Generate thumbnail using Thumbnailator
            Thumbnails.of(photoFile)
                    .size(MAX_THUMBNAIL_WIDTH, MAX_THUMBNAIL_HEIGHT)
                    .outputQuality(JPEG_QUALITY)
                    .toFile(thumbnailFile);

            logger.info("Generated thumbnail for {}: {}", photoFile.getName(), thumbnailFile.getAbsolutePath());
            return thumbnailFile.getAbsolutePath();

        } catch (IOException e) {
            logger.warn("Failed to generate thumbnail for {}: {}", photoFile.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error generating thumbnail for {}: {}", photoFile.getName(), e.getMessage(), e);
            return null;
        }
    }
}
