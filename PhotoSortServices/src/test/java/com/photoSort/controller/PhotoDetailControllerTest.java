/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.model.*;
import com.photoSort.repository.*;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test cases for Step 10: Image Display Page
 * Tests the photo detail endpoints functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PhotoDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ExifDataRepository exifDataRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PhotoTagRepository photoTagRepository;

    @Autowired
    private PhotoMetadataRepository photoMetadataRepository;

    @Autowired
    private MetadataFieldRepository metadataFieldRepository;

    private User owner;
    private Photo photo;
    private ExifData exifData;
    private Tag tag1;
    private Tag tag2;
    private MetadataField metadataField1;
    private PhotoMetadata photoMetadata1;

    @BeforeEach
    public void setUp() {
        // Clean up
        photoMetadataRepository.deleteAll();
        metadataFieldRepository.deleteAll();
        photoTagRepository.deleteAll();
        tagRepository.deleteAll();
        exifDataRepository.deleteAll();
        photoRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        owner = new User();
        owner.setGoogleId("owner123");
        owner.setEmail("owner@test.com");
        owner.setDisplayName("Photo Owner");
        owner.setUserType(User.UserType.USER);
        owner.setFirstLoginDate(LocalDateTime.now());
        owner.setLastLoginDate(LocalDateTime.now());
        owner = userRepository.save(owner);

        // Create test photo
        photo = new Photo();
        photo.setFileName("test-photo.jpg");
        photo.setFilePath("/test/path/test-photo.jpg");
        photo.setFileSize(1024000L);
        photo.setFileCreatedDate(LocalDateTime.now().minusDays(10));
        photo.setFileModifiedDate(LocalDateTime.now().minusDays(5));
        photo.setIsPublic(false);
        photo.setImageWidth(1920);
        photo.setImageHeight(1080);
        photo.setThumbnailPath("/test/path/thumbnails/test-photo-thumb.jpg");
        photo.setOwner(owner);
        photo = photoRepository.save(photo);

        // Create EXIF data
        exifData = new ExifData();
        exifData.setPhoto(photo);
        exifData.setDateTimeOriginal(LocalDateTime.now().minusDays(10));
        exifData.setCameraMake("Canon");
        exifData.setCameraModel("EOS 5D Mark IV");
        exifData.setGpsLatitude(new BigDecimal("37.7749"));
        exifData.setGpsLongitude(new BigDecimal("-122.4194"));
        exifData.setExposureTime("1/125");
        exifData.setFNumber("f/5.6");
        exifData.setIsoSpeed(400);
        exifData.setFocalLength("50mm");
        exifData.setOrientation(1);
        exifData = exifDataRepository.save(exifData);

        // Create tags
        tag1 = new Tag();
        tag1.setTagValue("landscape");
        tag1 = tagRepository.save(tag1);

        tag2 = new Tag();
        tag2.setTagValue("nature");
        tag2 = tagRepository.save(tag2);

        // Associate tags with photo
        PhotoTag photoTag1 = new PhotoTag();
        photoTag1.setPhoto(photo);
        photoTag1.setTag(tag1);
        photoTagRepository.save(photoTag1);

        PhotoTag photoTag2 = new PhotoTag();
        photoTag2.setPhoto(photo);
        photoTag2.setTag(tag2);
        photoTagRepository.save(photoTag2);

        // Create custom metadata field
        metadataField1 = new MetadataField();
        metadataField1.setFieldName("location");
        metadataField1 = metadataFieldRepository.save(metadataField1);

        // Create photo metadata
        photoMetadata1 = new PhotoMetadata();
        photoMetadata1.setPhoto(photo);
        photoMetadata1.setField(metadataField1);
        photoMetadata1.setMetadataValue("San Francisco");
        photoMetadata1 = photoMetadataRepository.save(photoMetadata1);
    }

    /**
     * Test Case 1: Get photo details returns complete photo information
     */
    @Test
    public void testImageDisplayPage_GetPhotoDetails() throws Exception {
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.photoId").value(photo.getPhotoId()))
                .andExpect(jsonPath("$.data.fileName").value("test-photo.jpg"))
                .andExpect(jsonPath("$.data.filePath").value("/test/path/test-photo.jpg"))
                .andExpect(jsonPath("$.data.imageWidth").value(1920))
                .andExpect(jsonPath("$.data.imageHeight").value(1080));
    }

    /**
     * Test Case 2: Get photo details includes EXIF data
     */
    @Test
    public void testImageDisplayPage_GetPhotoDetailsIncludesExif() throws Exception {
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exifData").exists())
                .andExpect(jsonPath("$.data.exifData.cameraMake").value("Canon"))
                .andExpect(jsonPath("$.data.exifData.cameraModel").value("EOS 5D Mark IV"))
                .andExpect(jsonPath("$.data.exifData.gpsLatitude").value(37.7749))
                .andExpect(jsonPath("$.data.exifData.gpsLongitude").value(-122.4194))
                .andExpect(jsonPath("$.data.exifData.exposureTime").value("1/125"))
                .andExpect(jsonPath("$.data.exifData.fNumber").value("f/5.6"))
                .andExpect(jsonPath("$.data.exifData.isoSpeed").value(400))
                .andExpect(jsonPath("$.data.exifData.focalLength").value("50mm"));
    }

    /**
     * Test Case 3: Get photo details includes custom metadata
     */
    @Test
    public void testImageDisplayPage_GetPhotoDetailsIncludesCustomMetadata() throws Exception {
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metadata").isArray())
                .andExpect(jsonPath("$.data.metadata", hasSize(1)))
                .andExpect(jsonPath("$.data.metadata[0].fieldName").value("location"))
                .andExpect(jsonPath("$.data.metadata[0].metadataValue").value("San Francisco"));
    }

    /**
     * Test Case 4: Get photo details includes tags
     */
    @Test
    public void testImageDisplayPage_GetPhotoDetailsIncludesTags() throws Exception {
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags").isArray())
                .andExpect(jsonPath("$.data.tags", hasSize(2)))
                .andExpect(jsonPath("$.data.tags[*].tagValue", containsInAnyOrder("landscape", "nature")));
    }

    /**
     * Test Case 5: Get photo details for non-existent photo returns 404
     */
    @Test
    public void testImageDisplayPage_GetPhotoDetails_NotFound() throws Exception {
        mockMvc.perform(get("/api/photos/99999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 6: Get photo image endpoint returns 404 when file doesn't exist
     */
    @Test
    public void testImageDisplayPage_GetPhotoImage() throws Exception {
        // Note: Test photo file doesn't exist on disk, so endpoint should return 404
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId() + "/image"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 7: Get photo image for non-existent photo returns 404
     */
    @Test
    public void testImageDisplayPage_GetPhotoImage_NotFound() throws Exception {
        mockMvc.perform(get("/api/photos/99999/image"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 8: Update photo metadata
     */
    @Test
    public void testImageDisplayPage_UpdatePhotoMetadata() throws Exception {
        String updatedMetadata = "[{\"fieldName\":\"location\",\"metadataValue\":\"Golden Gate Park\"}]";

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/metadata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMetadata))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify update persisted
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metadata[0].metadataValue").value("Golden Gate Park"));
    }

    /**
     * Test Case 9: Add new photo metadata field
     */
    @Test
    public void testImageDisplayPage_AddPhotoMetadata() throws Exception {
        String newMetadata = "[{\"fieldName\":\"location\",\"metadataValue\":\"San Francisco\"}," +
                             "{\"fieldName\":\"event\",\"metadataValue\":\"Vacation 2024\"}]";

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/metadata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newMetadata))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify both fields exist
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metadata", hasSize(2)))
                .andExpect(jsonPath("$.data.metadata[*].fieldName", containsInAnyOrder("location", "event")));
    }

    /**
     * Test Case 10: Delete photo metadata field
     */
    @Test
    public void testImageDisplayPage_DeletePhotoMetadata() throws Exception {
        String emptyMetadata = "[]";

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/metadata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyMetadata))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify metadata removed
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metadata", hasSize(0)));
    }

    /**
     * Test Case 11: Update photo tags
     */
    @Test
    public void testImageDisplayPage_UpdatePhotoTags() throws Exception {
        String updatedTags = "[\"landscape\",\"nature\",\"sunset\"]";

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTags))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify tags updated
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags", hasSize(3)))
                .andExpect(jsonPath("$.data.tags[*].tagValue", containsInAnyOrder("landscape", "nature", "sunset")));
    }

    /**
     * Test Case 12: Remove photo tags
     */
    @Test
    public void testImageDisplayPage_RemovePhotoTags() throws Exception {
        String updatedTags = "[\"landscape\"]";

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTags))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify only one tag remains
        mockMvc.perform(get("/api/photos/" + photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags", hasSize(1)))
                .andExpect(jsonPath("$.data.tags[0].tagValue").value("landscape"));
    }
}
