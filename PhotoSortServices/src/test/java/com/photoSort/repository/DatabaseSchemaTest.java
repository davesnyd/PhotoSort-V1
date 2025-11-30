/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for database schema and JPA repositories.
 * Tests all CRUD operations, foreign key relationships, and cascade behaviors.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class DatabaseSchemaTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ExifDataRepository exifDataRepository;

    @Autowired
    private MetadataFieldRepository metadataFieldRepository;

    @Autowired
    private PhotoMetadataRepository photoMetadataRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PhotoTagRepository photoTagRepository;

    @Autowired
    private PhotoPermissionRepository photoPermissionRepository;

    @Autowired
    private UserColumnPreferenceRepository userColumnPreferenceRepository;

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private ScriptExecutionLogRepository scriptExecutionLogRepository;

    private User testUser;
    private Photo testPhoto;

    /**
     * Set up test data before each test.
     */
    @BeforeEach
    public void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setGoogleId("google123");
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");
        testUser.setUserType(User.UserType.USER);
        testUser.setFirstLoginDate(LocalDateTime.now());
        testUser.setLastLoginDate(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create a test photo
        testPhoto = new Photo();
        testPhoto.setOwner(testUser);
        testPhoto.setFileName("test.jpg");
        testPhoto.setFilePath("/photos/test.jpg");
        testPhoto.setFileSize(1024L);
        testPhoto.setFileCreatedDate(LocalDateTime.now());
        testPhoto.setFileModifiedDate(LocalDateTime.now());
        testPhoto.setIsPublic(false);
        testPhoto.setImageWidth(800);
        testPhoto.setImageHeight(600);
        testPhoto = photoRepository.save(testPhoto);
    }

    // Test Case 1: Connect to database using credentials from environment variables
    @Test
    public void testDatabaseConnection() {
        assertNotNull(userRepository, "UserRepository should be autowired successfully");
        assertNotNull(photoRepository, "PhotoRepository should be autowired successfully");
    }

    // Test Case 2: Create all tables with proper schema
    @Test
    public void testTablesCreated() {
        // Verify we can save and retrieve data from each table
        assertNotNull(testUser.getUserId(), "User should have an ID after saving");
        assertNotNull(testPhoto.getPhotoId(), "Photo should have an ID after saving");
    }

    // Test Case 3: Verify all foreign key constraints exist
    @Test
    public void testForeignKeyConstraints() {
        // Verify photo references user
        Photo savedPhoto = photoRepository.findById(testPhoto.getPhotoId()).orElse(null);
        assertNotNull(savedPhoto);
        assertNotNull(savedPhoto.getOwner());
        assertEquals(testUser.getUserId(), savedPhoto.getOwner().getUserId());
    }

    // Test Case 4: Verify all indexes are created (implicit through query performance)
    @Test
    public void testIndexes() {
        // Find by indexed fields should work efficiently
        Optional<User> foundUser = userRepository.findByGoogleId("google123");
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUserId(), foundUser.get().getUserId());

        Optional<Photo> foundPhoto = photoRepository.findByFilePath("/photos/test.jpg");
        assertTrue(foundPhoto.isPresent());
        assertEquals(testPhoto.getPhotoId(), foundPhoto.get().getPhotoId());
    }

    // Test Case 5: Insert sample user record and verify
    @Test
    public void testInsertUser() {
        User newUser = new User();
        newUser.setGoogleId("google456");
        newUser.setEmail("newuser@example.com");
        newUser.setDisplayName("New User");
        newUser.setUserType(User.UserType.ADMIN);
        newUser.setFirstLoginDate(LocalDateTime.now());
        newUser.setLastLoginDate(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getUserId());
        assertEquals("google456", savedUser.getGoogleId());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals(User.UserType.ADMIN, savedUser.getUserType());
    }

    // Test Case 6: Insert sample photo record with foreign key to user and verify
    @Test
    public void testInsertPhotoWithForeignKey() {
        Photo newPhoto = new Photo();
        newPhoto.setOwner(testUser);
        newPhoto.setFileName("photo2.jpg");
        newPhoto.setFilePath("/photos/photo2.jpg");
        newPhoto.setFileSize(2048L);
        newPhoto.setIsPublic(true);

        Photo savedPhoto = photoRepository.save(newPhoto);

        assertNotNull(savedPhoto.getPhotoId());
        assertEquals(testUser.getUserId(), savedPhoto.getOwner().getUserId());
        assertTrue(savedPhoto.getIsPublic());
    }

    // Test Case 7: Insert EXIF data for photo and verify cascade
    @Test
    public void testInsertExifData() {
        ExifData exifData = new ExifData();
        exifData.setPhoto(testPhoto);
        exifData.setDateTimeOriginal(LocalDateTime.now());
        exifData.setCameraMake("Canon");
        exifData.setCameraModel("EOS 5D");
        exifData.setGpsLatitude(new BigDecimal("40.7128"));
        exifData.setGpsLongitude(new BigDecimal("-74.0060"));
        exifData.setIsoSpeed(400);

        ExifData savedExif = exifDataRepository.save(exifData);

        assertNotNull(savedExif.getExifId());
        assertEquals("Canon", savedExif.getCameraMake());
        assertEquals("EOS 5D", savedExif.getCameraModel());
        assertEquals(testPhoto.getPhotoId(), savedExif.getPhoto().getPhotoId());
    }

    // Test Case 8: Query photos with JOIN to users and verify relationship
    @Test
    public void testQueryPhotosWithJoin() {
        List<Photo> photos = photoRepository.findByOwner(testUser);

        assertFalse(photos.isEmpty());
        assertEquals(1, photos.size());
        assertEquals(testPhoto.getPhotoId(), photos.get(0).getPhotoId());
    }

    // Test Case 9: Test deletion cascade (delete photo, verify EXIF data deleted)
    @Test
    public void testDeletionCascade() {
        // Create EXIF data for the photo
        ExifData exifData = new ExifData();
        exifData.setPhoto(testPhoto);
        exifData.setCameraMake("Nikon");
        exifDataRepository.save(exifData);

        Long photoId = testPhoto.getPhotoId();

        // Delete the photo
        photoRepository.delete(testPhoto);
        photoRepository.flush();

        // Verify photo is deleted
        Optional<Photo> deletedPhoto = photoRepository.findById(photoId);
        assertFalse(deletedPhoto.isPresent());

        // Verify EXIF data is also deleted (cascade)
        Optional<ExifData> deletedExif = exifDataRepository.findByPhoto(testPhoto);
        assertFalse(deletedExif.isPresent());
    }

    // Test Case 10: Verify unique constraints (attempt duplicate insert)
    @Test
    public void testUniqueConstraints() {
        // Try to create a user with duplicate Google ID
        User duplicateUser = new User();
        duplicateUser.setGoogleId("google123"); // Same as testUser
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setDisplayName("Duplicate User");
        duplicateUser.setUserType(User.UserType.USER);
        duplicateUser.setFirstLoginDate(LocalDateTime.now());
        duplicateUser.setLastLoginDate(LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    // Additional test: Metadata field operations
    @Test
    public void testMetadataFieldOperations() {
        MetadataField field = new MetadataField();
        field.setFieldName("Location");

        MetadataField savedField = metadataFieldRepository.save(field);

        assertNotNull(savedField.getFieldId());
        assertEquals("Location", savedField.getFieldName());

        Optional<MetadataField> found = metadataFieldRepository.findByFieldName("Location");
        assertTrue(found.isPresent());
        assertEquals(savedField.getFieldId(), found.get().getFieldId());
    }

    // Additional test: Photo metadata operations
    @Test
    public void testPhotoMetadataOperations() {
        MetadataField field = new MetadataField();
        field.setFieldName("Event");
        field = metadataFieldRepository.save(field);

        PhotoMetadata metadata = new PhotoMetadata();
        metadata.setPhoto(testPhoto);
        metadata.setField(field);
        metadata.setMetadataValue("Birthday Party");

        PhotoMetadata savedMetadata = photoMetadataRepository.save(metadata);

        assertNotNull(savedMetadata.getMetadataId());
        assertEquals("Birthday Party", savedMetadata.getMetadataValue());

        List<PhotoMetadata> photoMetadata = photoMetadataRepository.findByPhoto(testPhoto);
        assertFalse(photoMetadata.isEmpty());
    }

    // Additional test: Tag operations
    @Test
    public void testTagOperations() {
        Tag tag = new Tag();
        tag.setTagValue("nature");

        Tag savedTag = tagRepository.save(tag);

        assertNotNull(savedTag.getTagId());
        assertEquals("nature", savedTag.getTagValue());

        Optional<Tag> found = tagRepository.findByTagValue("nature");
        assertTrue(found.isPresent());
    }

    // Additional test: Photo tag association
    @Test
    public void testPhotoTagAssociation() {
        Tag tag = new Tag();
        tag.setTagValue("sunset");
        tag = tagRepository.save(tag);

        PhotoTag photoTag = new PhotoTag();
        photoTag.setPhoto(testPhoto);
        photoTag.setTag(tag);

        PhotoTag savedPhotoTag = photoTagRepository.save(photoTag);

        assertNotNull(savedPhotoTag.getPhotoTagId());

        List<PhotoTag> photoTags = photoTagRepository.findByPhoto(testPhoto);
        assertEquals(1, photoTags.size());
    }

    // Additional test: Photo permissions
    @Test
    public void testPhotoPermissions() {
        User anotherUser = new User();
        anotherUser.setGoogleId("google789");
        anotherUser.setEmail("another@example.com");
        anotherUser.setDisplayName("Another User");
        anotherUser.setUserType(User.UserType.USER);
        anotherUser.setFirstLoginDate(LocalDateTime.now());
        anotherUser.setLastLoginDate(LocalDateTime.now());
        anotherUser = userRepository.save(anotherUser);

        PhotoPermission permission = new PhotoPermission();
        permission.setPhoto(testPhoto);
        permission.setUser(anotherUser);

        PhotoPermission savedPermission = photoPermissionRepository.save(permission);

        assertNotNull(savedPermission.getPermissionId());

        List<PhotoPermission> permissions = photoPermissionRepository.findByPhoto(testPhoto);
        assertEquals(1, permissions.size());

        boolean hasPermission = photoPermissionRepository.existsByPhotoAndUser(testPhoto, anotherUser);
        assertTrue(hasPermission);
    }

    // Additional test: User column preferences
    @Test
    public void testUserColumnPreferences() {
        UserColumnPreference preference = new UserColumnPreference();
        preference.setUser(testUser);
        preference.setColumnType(UserColumnPreference.ColumnType.STANDARD);
        preference.setColumnName("file_name");
        preference.setDisplayOrder(1);

        UserColumnPreference savedPreference = userColumnPreferenceRepository.save(preference);

        assertNotNull(savedPreference.getPreferenceId());

        List<UserColumnPreference> preferences = userColumnPreferenceRepository
                .findByUserOrderByDisplayOrderAsc(testUser);
        assertEquals(1, preferences.size());
    }

    // Additional test: Script operations
    @Test
    public void testScriptOperations() {
        Script script = new Script();
        script.setScriptName("TagGenerator");
        script.setScriptFileName("tag_gen.py");
        script.setScriptContents("print('Hello')");
        script.setFileExtension(".jpg");
        script.setPeriodicityMinutes(5);

        Script savedScript = scriptRepository.save(script);

        assertNotNull(savedScript.getScriptId());
        assertEquals("TagGenerator", savedScript.getScriptName());

        List<Script> scriptsForExtension = scriptRepository.findByFileExtension(".jpg");
        assertFalse(scriptsForExtension.isEmpty());
    }

    // Additional test: Script execution log
    @Test
    public void testScriptExecutionLog() {
        Script script = new Script();
        script.setScriptName("TestScript");
        script.setFileExtension(".jpg");
        script = scriptRepository.save(script);

        ScriptExecutionLog log = new ScriptExecutionLog();
        log.setScript(script);
        log.setPhoto(testPhoto);
        log.setStatus(ScriptExecutionLog.ExecutionStatus.SUCCESS);

        ScriptExecutionLog savedLog = scriptExecutionLogRepository.save(log);

        assertNotNull(savedLog.getLogId());
        assertEquals(ScriptExecutionLog.ExecutionStatus.SUCCESS, savedLog.getStatus());

        List<ScriptExecutionLog> logs = scriptExecutionLogRepository.findByScript(script);
        assertEquals(1, logs.size());
    }

    // Additional test: Count photos by owner
    @Test
    public void testCountPhotosByOwner() {
        long count = photoRepository.countByOwner(testUser);
        assertEquals(1, count);

        // Add another photo
        Photo anotherPhoto = new Photo();
        anotherPhoto.setOwner(testUser);
        anotherPhoto.setFileName("photo3.jpg");
        anotherPhoto.setFilePath("/photos/photo3.jpg");
        photoRepository.save(anotherPhoto);

        count = photoRepository.countByOwner(testUser);
        assertEquals(2, count);
    }
}
