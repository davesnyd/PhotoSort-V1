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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Step 20: Thumbnail Generation
 * Tests the ThumbnailService for generating photo thumbnails
 */
@SpringBootTest
@ActiveProfiles("test")
public class ThumbnailServiceTest {

    @Autowired(required = false)
    private ThumbnailService thumbnailService;

    @TempDir
    Path tempDir;

    private File testImageJpg;
    private File testImagePng;
    private File largeImage;
    private File wideImage;
    private File tallImage;

    @BeforeEach
    public void setUp() throws Exception {
        // Create test JPG image (100x100)
        testImageJpg = createTestImage("test.jpg", "jpg", 100, 100, Color.BLUE);

        // Create test PNG image (100x100)
        testImagePng = createTestImage("test.png", "png", 100, 100, Color.RED);

        // Create large image (800x600)
        largeImage = createTestImage("large.jpg", "jpg", 800, 600, Color.GREEN);

        // Create wide image (400x100)
        wideImage = createTestImage("wide.jpg", "jpg", 400, 100, Color.YELLOW);

        // Create tall image (100x400)
        tallImage = createTestImage("tall.jpg", "jpg", 100, 400, Color.MAGENTA);
    }

    /**
     * Test Case 1: Verify thumbnail generated for JPG image
     */
    @Test
    public void testThumbnailGeneration_JpgImage() {
        assertNotNull(thumbnailService, "ThumbnailService should be created");

        String thumbnailPath = thumbnailService.generateThumbnail(testImageJpg);

        assertNotNull(thumbnailPath, "Thumbnail path should not be null");
        File thumbnail = new File(thumbnailPath);
        assertTrue(thumbnail.exists(), "Thumbnail file should exist");
        assertTrue(thumbnail.isFile(), "Thumbnail should be a file");
    }

    /**
     * Test Case 2: Verify thumbnail generated for PNG image
     */
    @Test
    public void testThumbnailGeneration_PngImage() {
        assertNotNull(thumbnailService);

        String thumbnailPath = thumbnailService.generateThumbnail(testImagePng);

        assertNotNull(thumbnailPath, "Thumbnail path should not be null");
        File thumbnail = new File(thumbnailPath);
        assertTrue(thumbnail.exists(), "Thumbnail file should exist for PNG");
        assertTrue(thumbnail.getName().endsWith(".png"), "Thumbnail should maintain PNG format");
    }

    /**
     * Test Case 3: Verify thumbnail size max 200x200px
     */
    @Test
    public void testThumbnailGeneration_MaxSize200x200() throws Exception {
        assertNotNull(thumbnailService);

        // Generate thumbnail from large image
        String thumbnailPath = thumbnailService.generateThumbnail(largeImage);

        assertNotNull(thumbnailPath);
        File thumbnail = new File(thumbnailPath);
        BufferedImage thumbnailImage = ImageIO.read(thumbnail);

        int width = thumbnailImage.getWidth();
        int height = thumbnailImage.getHeight();

        assertTrue(width <= 200, "Thumbnail width should be <= 200px (was " + width + ")");
        assertTrue(height <= 200, "Thumbnail height should be <= 200px (was " + height + ")");
    }

    /**
     * Test Case 4: Verify aspect ratio maintained
     */
    @Test
    public void testThumbnailGeneration_AspectRatioMaintained() throws Exception {
        assertNotNull(thumbnailService);

        // Test wide image (4:1 aspect ratio)
        String wideThumbnailPath = thumbnailService.generateThumbnail(wideImage);
        BufferedImage wideThumbnail = ImageIO.read(new File(wideThumbnailPath));
        double wideAspect = (double) wideThumbnail.getWidth() / wideThumbnail.getHeight();
        assertTrue(wideAspect > 3.5, "Wide thumbnail should maintain wide aspect ratio (was " + wideAspect + ")");

        // Test tall image (1:4 aspect ratio)
        String tallThumbnailPath = thumbnailService.generateThumbnail(tallImage);
        BufferedImage tallThumbnail = ImageIO.read(new File(tallThumbnailPath));
        double tallAspect = (double) tallThumbnail.getWidth() / tallThumbnail.getHeight();
        assertTrue(tallAspect < 0.3, "Tall thumbnail should maintain tall aspect ratio (was " + tallAspect + ")");
    }

    /**
     * Test Case 5: Verify thumbnail saved to correct path
     */
    @Test
    public void testThumbnailGeneration_SavedToCorrectPath() {
        assertNotNull(thumbnailService);

        String thumbnailPath = thumbnailService.generateThumbnail(testImageJpg);

        assertNotNull(thumbnailPath);
        assertTrue(thumbnailPath.contains("thumbnails"), "Thumbnail path should contain 'thumbnails' directory");
        assertTrue(thumbnailPath.contains("_thumb"), "Thumbnail filename should contain '_thumb'");
    }

    /**
     * Test Case 6: Verify thumbnail path returned correctly
     */
    @Test
    public void testThumbnailGeneration_PathReturned() {
        assertNotNull(thumbnailService);

        String thumbnailPath = thumbnailService.generateThumbnail(testImageJpg);

        assertNotNull(thumbnailPath, "Thumbnail path should be returned");
        assertFalse(thumbnailPath.isEmpty(), "Thumbnail path should not be empty");
        assertTrue(thumbnailPath.endsWith(".jpg") || thumbnailPath.endsWith(".png"),
            "Thumbnail path should have image extension");
    }

    /**
     * Test Case 7: Verify handling of corrupt image file
     */
    @Test
    public void testThumbnailGeneration_CorruptImageFile() throws Exception {
        assertNotNull(thumbnailService);

        // Create corrupt "image" file (just text)
        File corruptFile = new File(tempDir.toFile(), "corrupt.jpg");
        try (FileWriter writer = new FileWriter(corruptFile)) {
            writer.write("This is not an image");
        }

        // Should handle gracefully and return null or throw exception
        String thumbnailPath = thumbnailService.generateThumbnail(corruptFile);

        // Accept either null return or exception (graceful failure)
        // If null, test passes; if exception thrown, test should catch it
        assertTrue(thumbnailPath == null || thumbnailPath.isEmpty(),
            "Corrupt image should return null or empty path");
    }

    /**
     * Test Case 8: Verify handling of unsupported image format
     */
    @Test
    public void testThumbnailGeneration_UnsupportedFormat() throws Exception {
        assertNotNull(thumbnailService);

        // Create file with unsupported extension
        File unsupportedFile = new File(tempDir.toFile(), "unsupported.xyz");
        try (FileWriter writer = new FileWriter(unsupportedFile)) {
            writer.write("Not a real image");
        }

        // Should handle gracefully
        String thumbnailPath = thumbnailService.generateThumbnail(unsupportedFile);

        assertTrue(thumbnailPath == null || thumbnailPath.isEmpty(),
            "Unsupported format should return null or empty path");
    }

    /**
     * Test Case 9: Verify thumbnail quality acceptable
     * Quality is subjective, but we verify it's a valid image
     */
    @Test
    public void testThumbnailGeneration_QualityAcceptable() throws Exception {
        assertNotNull(thumbnailService);

        String thumbnailPath = thumbnailService.generateThumbnail(largeImage);

        assertNotNull(thumbnailPath);
        BufferedImage thumbnail = ImageIO.read(new File(thumbnailPath));

        assertNotNull(thumbnail, "Thumbnail should be a valid readable image");
        assertTrue(thumbnail.getWidth() > 0, "Thumbnail should have positive width");
        assertTrue(thumbnail.getHeight() > 0, "Thumbnail should have positive height");
    }

    /**
     * Test Case 10: Verify thumbnail file size reasonable (< 50KB)
     */
    @Test
    public void testThumbnailGeneration_FileSizeReasonable() throws Exception {
        assertNotNull(thumbnailService);

        String thumbnailPath = thumbnailService.generateThumbnail(largeImage);

        assertNotNull(thumbnailPath);
        File thumbnail = new File(thumbnailPath);
        long fileSize = thumbnail.length();

        assertTrue(fileSize < 50 * 1024, "Thumbnail should be < 50KB (was " + fileSize + " bytes)");
        assertTrue(fileSize > 100, "Thumbnail should be > 100 bytes (sanity check)");
    }

    // Helper method to create test images
    private File createTestImage(String filename, String format, int width, int height, Color color) throws Exception {
        File imageFile = new File(tempDir.toFile(), filename);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ImageIO.write(image, format, imageFile);

        return imageFile;
    }
}
