/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.repository;

import com.photoSort.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Step 3: Database Connection Configuration.
 * Validates database connectivity, entity mapping, and transaction management.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class DatabaseConnectionConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestEntityManager entityManager;

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

    // Test Case 1: Verify database connection successful on startup
    @Test
    public void testDatabaseConnectionSuccessful() throws Exception {
        assertNotNull(dataSource, "DataSource should be autowired");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should be established");
            assertFalse(connection.isClosed(), "Connection should be open");

            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            assertEquals("PostgreSQL", databaseProductName, "Should be connected to PostgreSQL");
        }
    }

    // Test Case 2: Verify all entity classes map correctly to database tables
    @Test
    public void testEntityClassesMappedCorrectly() {
        // Verify each entity can be persisted, which confirms correct mapping
        User user = new User();
        user.setGoogleId("test_google_id");
        user.setEmail("test@test.com");
        user.setDisplayName("Test User");
        user.setUserType(User.UserType.USER);
        user.setFirstLoginDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());

        User savedUser = entityManager.persistAndFlush(user);
        assertNotNull(savedUser.getUserId(), "User entity should be mapped correctly");

        Photo photo = new Photo();
        photo.setOwner(savedUser);
        photo.setFileName("test.jpg");
        photo.setFilePath("/test/test.jpg");

        Photo savedPhoto = entityManager.persistAndFlush(photo);
        assertNotNull(savedPhoto.getPhotoId(), "Photo entity should be mapped correctly");

        ExifData exifData = new ExifData();
        exifData.setPhoto(savedPhoto);
        exifData.setCameraMake("Canon");

        ExifData savedExif = entityManager.persistAndFlush(exifData);
        assertNotNull(savedExif.getExifId(), "ExifData entity should be mapped correctly");

        MetadataField field = new MetadataField();
        field.setFieldName("TestField");

        MetadataField savedField = entityManager.persistAndFlush(field);
        assertNotNull(savedField.getFieldId(), "MetadataField entity should be mapped correctly");

        PhotoMetadata metadata = new PhotoMetadata();
        metadata.setPhoto(savedPhoto);
        metadata.setField(savedField);
        metadata.setMetadataValue("TestValue");

        PhotoMetadata savedMetadata = entityManager.persistAndFlush(metadata);
        assertNotNull(savedMetadata.getMetadataId(), "PhotoMetadata entity should be mapped correctly");

        Tag tag = new Tag();
        tag.setTagValue("test-tag");

        Tag savedTag = entityManager.persistAndFlush(tag);
        assertNotNull(savedTag.getTagId(), "Tag entity should be mapped correctly");

        PhotoTag photoTag = new PhotoTag();
        photoTag.setPhoto(savedPhoto);
        photoTag.setTag(savedTag);

        PhotoTag savedPhotoTag = entityManager.persistAndFlush(photoTag);
        assertNotNull(savedPhotoTag.getPhotoTagId(), "PhotoTag entity should be mapped correctly");

        User anotherUser = new User();
        anotherUser.setGoogleId("another_google_id");
        anotherUser.setEmail("another@test.com");
        anotherUser.setDisplayName("Another User");
        anotherUser.setUserType(User.UserType.USER);
        anotherUser.setFirstLoginDate(LocalDateTime.now());
        anotherUser.setLastLoginDate(LocalDateTime.now());
        User savedAnotherUser = entityManager.persistAndFlush(anotherUser);

        PhotoPermission permission = new PhotoPermission();
        permission.setPhoto(savedPhoto);
        permission.setUser(savedAnotherUser);

        PhotoPermission savedPermission = entityManager.persistAndFlush(permission);
        assertNotNull(savedPermission.getPermissionId(), "PhotoPermission entity should be mapped correctly");

        UserColumnPreference preference = new UserColumnPreference();
        preference.setUser(savedUser);
        preference.setColumnType(UserColumnPreference.ColumnType.STANDARD);
        preference.setColumnName("file_name");
        preference.setDisplayOrder(1);

        UserColumnPreference savedPreference = entityManager.persistAndFlush(preference);
        assertNotNull(savedPreference.getPreferenceId(), "UserColumnPreference entity should be mapped correctly");

        Script script = new Script();
        script.setScriptName("TestScript");
        script.setFileExtension(".jpg");

        Script savedScript = entityManager.persistAndFlush(script);
        assertNotNull(savedScript.getScriptId(), "Script entity should be mapped correctly");

        ScriptExecutionLog log = new ScriptExecutionLog();
        log.setScript(savedScript);
        log.setPhoto(savedPhoto);
        log.setStatus(ScriptExecutionLog.ExecutionStatus.SUCCESS);

        ScriptExecutionLog savedLog = entityManager.persistAndFlush(log);
        assertNotNull(savedLog.getLogId(), "ScriptExecutionLog entity should be mapped correctly");
    }

    // Test Case 3: Create JPA repositories for each entity (verify they exist)
    @Test
    public void testJpaRepositoriesExist() {
        assertNotNull(userRepository, "UserRepository should exist");
        assertNotNull(photoRepository, "PhotoRepository should exist");
        assertNotNull(exifDataRepository, "ExifDataRepository should exist");
        assertNotNull(metadataFieldRepository, "MetadataFieldRepository should exist");
        assertNotNull(photoMetadataRepository, "PhotoMetadataRepository should exist");
        assertNotNull(tagRepository, "TagRepository should exist");
        assertNotNull(photoTagRepository, "PhotoTagRepository should exist");
        assertNotNull(photoPermissionRepository, "PhotoPermissionRepository should exist");
        assertNotNull(userColumnPreferenceRepository, "UserColumnPreferenceRepository should exist");
        assertNotNull(scriptRepository, "ScriptRepository should exist");
        assertNotNull(scriptExecutionLogRepository, "ScriptExecutionLogRepository should exist");
    }

    // Test Case 4: Test CRUD operations on User entity
    @Test
    public void testUserCrudOperations() {
        // Create
        User user = new User();
        user.setGoogleId("crud_test_google");
        user.setEmail("crud@test.com");
        user.setDisplayName("CRUD Test User");
        user.setUserType(User.UserType.USER);
        user.setFirstLoginDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getUserId(), "User should be created with ID");

        // Read
        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals("crud@test.com", foundUser.get().getEmail(), "Email should match");

        // Update
        foundUser.get().setDisplayName("Updated Name");
        User updatedUser = userRepository.save(foundUser.get());
        assertEquals("Updated Name", updatedUser.getDisplayName(), "Display name should be updated");

        // Delete
        Long userId = updatedUser.getUserId();
        userRepository.delete(updatedUser);
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent(), "User should be deleted");
    }

    // Test Case 5: Test CRUD operations on Photo entity with foreign key to User
    @Test
    public void testPhotoCrudWithForeignKey() {
        // Create user first
        User user = new User();
        user.setGoogleId("photo_test_google");
        user.setEmail("photo@test.com");
        user.setDisplayName("Photo Test User");
        user.setUserType(User.UserType.USER);
        user.setFirstLoginDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());
        user = userRepository.save(user);

        // Create photo with foreign key
        Photo photo = new Photo();
        photo.setOwner(user);
        photo.setFileName("foreign_key_test.jpg");
        photo.setFilePath("/test/foreign_key_test.jpg");
        photo.setFileSize(1024L);
        photo.setIsPublic(false);

        Photo savedPhoto = photoRepository.save(photo);
        assertNotNull(savedPhoto.getPhotoId(), "Photo should be created with ID");
        assertNotNull(savedPhoto.getOwner(), "Photo should have owner");
        assertEquals(user.getUserId(), savedPhoto.getOwner().getUserId(), "Owner should match");

        // Read with JOIN
        Photo foundPhoto = photoRepository.findById(savedPhoto.getPhotoId()).orElse(null);
        assertNotNull(foundPhoto);
        assertEquals(user.getUserId(), foundPhoto.getOwner().getUserId(), "Foreign key should be maintained");

        // Update
        foundPhoto.setFileName("updated_name.jpg");
        Photo updatedPhoto = photoRepository.save(foundPhoto);
        assertEquals("updated_name.jpg", updatedPhoto.getFileName(), "File name should be updated");

        // Delete
        photoRepository.delete(updatedPhoto);
        assertFalse(photoRepository.findById(savedPhoto.getPhotoId()).isPresent(), "Photo should be deleted");
    }

    // Test Case 6: Test cascade operations (delete photo, verify EXIF data deleted)
    @Test
    public void testCascadeOperations() {
        // Create user
        User user = new User();
        user.setGoogleId("cascade_test_google");
        user.setEmail("cascade@test.com");
        user.setDisplayName("Cascade Test User");
        user.setUserType(User.UserType.USER);
        user.setFirstLoginDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());
        user = userRepository.save(user);

        // Create photo
        Photo photo = new Photo();
        photo.setOwner(user);
        photo.setFileName("cascade_test.jpg");
        photo.setFilePath("/test/cascade_test.jpg");
        photo = photoRepository.save(photo);

        // Create EXIF data
        ExifData exifData = new ExifData();
        exifData.setPhoto(photo);
        exifData.setCameraMake("Canon");
        exifData.setCameraModel("EOS 5D");
        exifData = exifDataRepository.save(exifData);

        Long photoId = photo.getPhotoId();
        Long exifId = exifData.getExifId();

        // Delete photo
        photoRepository.delete(photo);
        photoRepository.flush();

        // Verify cascade: photo and EXIF data should be deleted
        assertFalse(photoRepository.findById(photoId).isPresent(), "Photo should be deleted");
        assertFalse(exifDataRepository.findById(exifId).isPresent(), "EXIF data should be cascade deleted");
    }

    // Test Case 7: Verify Hibernate generates correct SQL queries
    @Test
    public void testHibernateGeneratesSql() {
        // This test verifies Hibernate is working by performing a query
        // SQL logging is enabled in application-test.properties
        User user = new User();
        user.setGoogleId("sql_test_google");
        user.setEmail("sql@test.com");
        user.setDisplayName("SQL Test User");
        user.setUserType(User.UserType.USER);
        user.setFirstLoginDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Force SQL generation by clearing and finding
        entityManager.flush();
        entityManager.clear();

        Optional<User> foundUser = userRepository.findByGoogleId("sql_test_google");
        assertTrue(foundUser.isPresent(), "User should be found via generated SQL");
        assertEquals(savedUser.getUserId(), foundUser.get().getUserId(), "Found user should match saved user");
    }

    // Test Case 8: Test transaction rollback on error
    @Test
    public void testTransactionRollback() {
        // Create a user
        User user = new User();
        user.setGoogleId("rollback_test_google");
        user.setEmail("rollback@test.com");
        user.setDisplayName("Rollback Test User");
        user.setUserType(User.UserType.USER);
        user.setFirstLoginDate(LocalDateTime.now());
        user.setLastLoginDate(LocalDateTime.now());

        userRepository.save(user);

        long initialCount = userRepository.count();

        // Try to create a duplicate user (should fail due to unique constraint)
        User duplicateUser = new User();
        duplicateUser.setGoogleId("rollback_test_google"); // Duplicate!
        duplicateUser.setEmail("different@test.com");
        duplicateUser.setDisplayName("Duplicate User");
        duplicateUser.setUserType(User.UserType.USER);
        duplicateUser.setFirstLoginDate(LocalDateTime.now());
        duplicateUser.setLastLoginDate(LocalDateTime.now());

        // This should throw an exception
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });

        // Verify transaction was rolled back - count should not change
        entityManager.clear(); // Clear the persistence context
        long finalCount = userRepository.count();
        assertEquals(initialCount, finalCount, "Transaction should be rolled back, count unchanged");
    }
}
