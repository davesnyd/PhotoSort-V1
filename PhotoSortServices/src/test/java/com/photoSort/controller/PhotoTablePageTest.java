/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.model.Photo;
import com.photoSort.model.PhotoPermission;
import com.photoSort.model.User;
import com.photoSort.model.UserColumnPreference;
import com.photoSort.repository.PhotoPermissionRepository;
import com.photoSort.repository.PhotoRepository;
import com.photoSort.repository.UserColumnPreferenceRepository;
import com.photoSort.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Photo Table Page (Step 7).
 * Tests the GET /api/photos endpoint with various permission scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PhotoTablePageTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoPermissionRepository photoPermissionRepository;

    @Autowired
    private UserColumnPreferenceRepository userColumnPreferenceRepository;

    private User regularUser;
    private User adminUser;
    private User otherUser;

    @BeforeEach
    public void setUp() {
        // Clean up database
        photoPermissionRepository.deleteAll();
        photoRepository.deleteAll();
        userColumnPreferenceRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        regularUser = new User();
        regularUser.setGoogleId("regular-google-id");
        regularUser.setEmail("regular@example.com");
        regularUser.setDisplayName("Regular User");
        regularUser.setUserType(User.UserType.USER);
        regularUser.setFirstLoginDate(LocalDateTime.now());
        regularUser.setLastLoginDate(LocalDateTime.now());
        regularUser = userRepository.save(regularUser);

        adminUser = new User();
        adminUser.setGoogleId("admin-google-id");
        adminUser.setEmail("admin@example.com");
        adminUser.setDisplayName("Admin User");
        adminUser.setUserType(User.UserType.ADMIN);
        adminUser.setFirstLoginDate(LocalDateTime.now());
        adminUser.setLastLoginDate(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        otherUser = new User();
        otherUser.setGoogleId("other-google-id");
        otherUser.setEmail("other@example.com");
        otherUser.setDisplayName("Other User");
        otherUser.setUserType(User.UserType.USER);
        otherUser.setFirstLoginDate(LocalDateTime.now());
        otherUser.setLastLoginDate(LocalDateTime.now());
        otherUser = userRepository.save(otherUser);
    }

    /**
     * Test Case 1: Verify user sees only authorized photos (owned, public, or granted access)
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_UserSeesOnlyAuthorizedPhotos() throws Exception {
        // Create photos owned by regular user
        Photo ownedPhoto = createPhoto(regularUser, "owned.jpg", "/photos/owned.jpg", false);

        // Create public photo owned by other user
        Photo publicPhoto = createPhoto(otherUser, "public.jpg", "/photos/public.jpg", true);

        // Create private photo owned by other user with permission granted
        Photo grantedPhoto = createPhoto(otherUser, "granted.jpg", "/photos/granted.jpg", false);
        grantPhotoPermission(grantedPhoto, regularUser);

        // Create private photo owned by other user without permission
        Photo deniedPhoto = createPhoto(otherUser, "denied.jpg", "/photos/denied.jpg", false);

        mockMvc.perform(get("/api/photos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[*].fileName", hasItems("owned.jpg", "public.jpg", "granted.jpg")))
                .andExpect(jsonPath("$.data.content[*].fileName", not(hasItem("denied.jpg"))));
    }

    /**
     * Test Case 2: Verify admin sees all photos (or filtered by user if parameter provided)
     */
    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    public void testPhotoTablePage_AdminSeesAllPhotos() throws Exception {
        // Create photos for different users
        Photo photo1 = createPhoto(regularUser, "photo1.jpg", "/photos/photo1.jpg", false);
        Photo photo2 = createPhoto(otherUser, "photo2.jpg", "/photos/photo2.jpg", false);
        Photo photo3 = createPhoto(adminUser, "photo3.jpg", "/photos/photo3.jpg", true);

        mockMvc.perform(get("/api/photos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[*].fileName", hasItems("photo1.jpg", "photo2.jpg", "photo3.jpg")));
    }

    /**
     * Test Case 2b: Verify admin can filter photos by specific user
     */
    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    public void testPhotoTablePage_AdminFiltersPhotosByUser() throws Exception {
        // Create photos for different users
        Photo photo1 = createPhoto(regularUser, "regular1.jpg", "/photos/regular1.jpg", false);
        Photo photo2 = createPhoto(regularUser, "regular2.jpg", "/photos/regular2.jpg", true);
        Photo photo3 = createPhoto(otherUser, "other.jpg", "/photos/other.jpg", false);

        mockMvc.perform(get("/api/photos")
                        .param("userId", regularUser.getUserId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].fileName", hasItems("regular1.jpg", "regular2.jpg")))
                .andExpect(jsonPath("$.data.content[*].fileName", not(hasItem("other.jpg"))));
    }

    /**
     * Test Case 3: Verify user's custom columns from preferences are displayed
     * (This is a backend test to ensure the API returns column preference data)
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_UserCustomColumnsReturned() throws Exception {
        // Create column preferences for regular user
        createColumnPreference(regularUser, UserColumnPreference.ColumnType.STANDARD, "file_name", 1);
        createColumnPreference(regularUser, UserColumnPreference.ColumnType.STANDARD, "file_created_date", 2);
        createColumnPreference(regularUser, UserColumnPreference.ColumnType.METADATA, "camera_make", 3);

        mockMvc.perform(get("/api/users/" + regularUser.getUserId() + "/columns")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[*].columnName", hasItems("file_name", "file_created_date", "camera_make")));
    }

    /**
     * Test Case 4: Verify default columns shown for new users
     * (Backend should return default columns if user has no preferences)
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_DefaultColumnsForNewUser() throws Exception {
        // No column preferences created - should return defaults
        mockMvc.perform(get("/api/users/" + regularUser.getUserId() + "/columns")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[*].columnName", hasItems("file_name", "file_created_date", "thumbnail")));
    }

    /**
     * Test Case 13: Verify column sorting works (ascending/descending)
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_ColumnSortingWorks() throws Exception {
        // Create photos with different names
        createPhoto(regularUser, "zebra.jpg", "/photos/zebra.jpg", false);
        createPhoto(regularUser, "apple.jpg", "/photos/apple.jpg", false);
        createPhoto(regularUser, "mango.jpg", "/photos/mango.jpg", false);

        // Test ascending sort
        mockMvc.perform(get("/api/photos")
                        .param("sort", "fileName")
                        .param("direction", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].fileName").value("apple.jpg"))
                .andExpect(jsonPath("$.data.content[1].fileName").value("mango.jpg"))
                .andExpect(jsonPath("$.data.content[2].fileName").value("zebra.jpg"));

        // Test descending sort
        mockMvc.perform(get("/api/photos")
                        .param("sort", "fileName")
                        .param("direction", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].fileName").value("zebra.jpg"))
                .andExpect(jsonPath("$.data.content[1].fileName").value("mango.jpg"))
                .andExpect(jsonPath("$.data.content[2].fileName").value("apple.jpg"));
    }

    /**
     * Test Case 14: Verify pagination controls work correctly
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_PaginationWorks() throws Exception {
        // Create 25 photos
        for (int i = 1; i <= 25; i++) {
            createPhoto(regularUser, "photo" + i + ".jpg", "/photos/photo" + i + ".jpg", false);
        }

        // Test first page with page size 10
        mockMvc.perform(get("/api/photos")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(10)))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(25));

        // Test second page
        mockMvc.perform(get("/api/photos")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(10)))
                .andExpect(jsonPath("$.data.page").value(1));
    }

    /**
     * Test Case 10: Verify quick search works across visible columns
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_QuickSearchWorks() throws Exception {
        // Create photos with different names
        createPhoto(regularUser, "vacation2024.jpg", "/photos/vacation2024.jpg", false);
        createPhoto(regularUser, "family.jpg", "/photos/family.jpg", false);
        createPhoto(regularUser, "vacation2023.jpg", "/photos/vacation2023.jpg", false);

        // Search for "vacation"
        mockMvc.perform(get("/api/photos")
                        .param("search", "vacation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].fileName", hasItems("vacation2024.jpg", "vacation2023.jpg")))
                .andExpect(jsonPath("$.data.content[*].fileName", not(hasItem("family.jpg"))));
    }

    /**
     * Test Case 15: Verify public photos visible to all users
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_PublicPhotosVisibleToAll() throws Exception {
        // Create public photos owned by other users
        Photo publicPhoto1 = createPhoto(otherUser, "public1.jpg", "/photos/public1.jpg", true);
        Photo publicPhoto2 = createPhoto(adminUser, "public2.jpg", "/photos/public2.jpg", true);

        // Create private photo (should not be visible)
        Photo privatePhoto = createPhoto(otherUser, "private.jpg", "/photos/private.jpg", false);

        mockMvc.perform(get("/api/photos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].fileName", hasItems("public1.jpg", "public2.jpg")))
                .andExpect(jsonPath("$.data.content[*].fileName", not(hasItem("private.jpg"))));
    }

    /**
     * Test Case 11: Verify advanced search allows selection of columns with must/must not contain
     */
    @Test
    @WithMockUser(username = "regular@example.com", roles = {"USER"})
    public void testPhotoTablePage_AdvancedSearchWorks() throws Exception {
        // Create photos with varying attributes
        Photo photo1 = createPhoto(regularUser, "beach_sunset.jpg", "/photos/beach_sunset.jpg", false);
        Photo photo2 = createPhoto(regularUser, "beach_morning.jpg", "/photos/beach_morning.jpg", false);
        Photo photo3 = createPhoto(regularUser, "mountain_sunset.jpg", "/photos/mountain_sunset.jpg", false);

        // Advanced search: fileName must contain "beach" AND must not contain "morning"
        mockMvc.perform(get("/api/photos")
                        .param("filterField1", "fileName")
                        .param("filterValue1", "beach")
                        .param("filterType1", "MUST_CONTAIN")
                        .param("filterField2", "fileName")
                        .param("filterValue2", "morning")
                        .param("filterType2", "MUST_NOT_CONTAIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].fileName").value("beach_sunset.jpg"));
    }

    // Helper methods

    private Photo createPhoto(User owner, String fileName, String filePath, boolean isPublic) {
        Photo photo = new Photo();
        photo.setOwner(owner);
        photo.setFileName(fileName);
        photo.setFilePath(filePath);
        photo.setFileSize(1024L);
        photo.setFileCreatedDate(LocalDateTime.now().minusDays(1));
        photo.setFileModifiedDate(LocalDateTime.now());
        photo.setIsPublic(isPublic);
        photo.setImageWidth(1920);
        photo.setImageHeight(1080);
        photo.setThumbnailPath("/thumbnails/" + fileName);
        return photoRepository.save(photo);
    }

    private void grantPhotoPermission(Photo photo, User user) {
        PhotoPermission permission = new PhotoPermission();
        permission.setPhoto(photo);
        permission.setUser(user);
        photoPermissionRepository.save(permission);
    }

    private void createColumnPreference(User user, UserColumnPreference.ColumnType columnType, String columnName, int displayOrder) {
        UserColumnPreference pref = new UserColumnPreference();
        pref.setUser(user);
        pref.setColumnType(columnType);
        pref.setColumnName(columnName);
        pref.setDisplayOrder(displayOrder);
        userColumnPreferenceRepository.save(pref);
    }
}
