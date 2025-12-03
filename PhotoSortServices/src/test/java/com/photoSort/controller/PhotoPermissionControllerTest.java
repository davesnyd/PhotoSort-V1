/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.model.Photo;
import com.photoSort.model.PhotoPermission;
import com.photoSort.model.User;
import com.photoSort.repository.PhotoPermissionRepository;
import com.photoSort.repository.PhotoRepository;
import com.photoSort.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test cases for Step 9: User Access Dialog
 * Tests the photo permission endpoints functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PhotoPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoPermissionRepository photoPermissionRepository;

    private User owner;
    private User user1;
    private User user2;
    private User user3;
    private Photo photo;

    @BeforeEach
    public void setUp() {
        // Clean up
        photoPermissionRepository.deleteAll();
        photoRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        owner = new User();
        owner.setGoogleId("owner123");
        owner.setEmail("owner@test.com");
        owner.setDisplayName("Photo Owner");
        owner.setUserType(User.UserType.USER);
        owner.setFirstLoginDate(LocalDateTime.now());
        owner.setLastLoginDate(LocalDateTime.now());
        owner = userRepository.save(owner);

        user1 = new User();
        user1.setGoogleId("user1");
        user1.setEmail("user1@test.com");
        user1.setDisplayName("User One");
        user1.setUserType(User.UserType.USER);
        user1.setFirstLoginDate(LocalDateTime.now());
        user1.setLastLoginDate(LocalDateTime.now());
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setGoogleId("user2");
        user2.setEmail("user2@test.com");
        user2.setDisplayName("User Two");
        user2.setUserType(User.UserType.USER);
        user2.setFirstLoginDate(LocalDateTime.now());
        user2.setLastLoginDate(LocalDateTime.now());
        user2 = userRepository.save(user2);

        user3 = new User();
        user3.setGoogleId("user3");
        user3.setEmail("user3@test.com");
        user3.setDisplayName("User Three");
        user3.setUserType(User.UserType.USER);
        user3.setFirstLoginDate(LocalDateTime.now());
        user3.setLastLoginDate(LocalDateTime.now());
        user3 = userRepository.save(user3);

        // Create test photo
        photo = new Photo();
        photo.setFileName("test-photo.jpg");
        photo.setFilePath("/test/path/test-photo.jpg");
        photo.setFileSize(1024L);
        photo.setFileCreatedDate(LocalDateTime.now());
        photo.setFileModifiedDate(LocalDateTime.now());
        photo.setOwner(owner);
        photo = photoRepository.save(photo);

        // Grant permission to user1
        PhotoPermission permission = new PhotoPermission();
        permission.setPhoto(photo);
        permission.setUser(user1);
        photoPermissionRepository.save(permission);
    }

    /**
     * Test Case 1: Get permissions returns users with access
     */
    @Test
    public void testUserAccessDialog_GetPermissions() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/photos/" + photo.getPhotoId() + "/permissions"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<List<Long>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<Long>>>() {});

        assertTrue(response.isSuccess(), "Response should be successful");
        assertNotNull(response.getData(), "Data should not be null");

        List<Long> userIds = response.getData();
        assertEquals(1, userIds.size(), "Should have 1 user with permission");
        assertTrue(userIds.contains(user1.getUserId()), "Should include user1");
        assertFalse(userIds.contains(user2.getUserId()), "Should not include user2");
    }

    /**
     * Test Case 2: Get permissions for photo with no permissions returns empty list
     */
    @Test
    public void testUserAccessDialog_GetPermissions_NoneGranted() throws Exception {
        // Create photo with no permissions
        Photo photo2 = new Photo();
        photo2.setFileName("no-perms.jpg");
        photo2.setFilePath("/test/no-perms.jpg");
        photo2.setFileSize(512L);
        photo2.setFileCreatedDate(LocalDateTime.now());
        photo2.setFileModifiedDate(LocalDateTime.now());
        photo2.setOwner(owner);
        photo2 = photoRepository.save(photo2);

        MvcResult result = mockMvc.perform(get("/api/photos/" + photo2.getPhotoId() + "/permissions"))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<List<Long>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<Long>>>() {});

        assertTrue(response.isSuccess(), "Response should be successful");
        List<Long> userIds = response.getData();
        assertTrue(userIds.isEmpty(), "Should have no permissions");
    }

    /**
     * Test Case 3: Update permissions adds new users
     */
    @Test
    public void testUserAccessDialog_UpdatePermissions_AddUsers() throws Exception {
        // Grant access to user2 and user3 (user1 already has access)
        List<Long> newPermissions = Arrays.asList(user1.getUserId(), user2.getUserId(), user3.getUserId());

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPermissions)))
                .andExpect(status().isOk());

        // Verify permissions in database
        List<PhotoPermission> permissions = photoPermissionRepository.findAll().stream()
                .filter(p -> p.getPhoto().getPhotoId().equals(photo.getPhotoId()))
                .toList();

        assertEquals(3, permissions.size(), "Should have 3 permissions");
        assertTrue(permissions.stream().anyMatch(p -> p.getUser().getUserId().equals(user1.getUserId())));
        assertTrue(permissions.stream().anyMatch(p -> p.getUser().getUserId().equals(user2.getUserId())));
        assertTrue(permissions.stream().anyMatch(p -> p.getUser().getUserId().equals(user3.getUserId())));
    }

    /**
     * Test Case 4: Update permissions removes users
     */
    @Test
    public void testUserAccessDialog_UpdatePermissions_RemoveUsers() throws Exception {
        // Remove user1's access (empty list)
        List<Long> newPermissions = Arrays.asList();

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPermissions)))
                .andExpect(status().isOk());

        // Verify permissions removed from database
        List<PhotoPermission> permissions = photoPermissionRepository.findAll().stream()
                .filter(p -> p.getPhoto().getPhotoId().equals(photo.getPhotoId()))
                .toList();

        assertEquals(0, permissions.size(), "Should have no permissions");
    }

    /**
     * Test Case 5: Update permissions replaces old with new
     */
    @Test
    public void testUserAccessDialog_UpdatePermissions_ReplaceUsers() throws Exception {
        // Replace user1 with user2
        List<Long> newPermissions = Arrays.asList(user2.getUserId());

        mockMvc.perform(put("/api/photos/" + photo.getPhotoId() + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPermissions)))
                .andExpect(status().isOk());

        // Verify permissions updated
        List<PhotoPermission> permissions = photoPermissionRepository.findAll().stream()
                .filter(p -> p.getPhoto().getPhotoId().equals(photo.getPhotoId()))
                .toList();

        assertEquals(1, permissions.size(), "Should have 1 permission");
        assertEquals(user2.getUserId(), permissions.get(0).getUser().getUserId(),
                "Should be user2");
    }

    /**
     * Test Case 6: Get permissions for non-existent photo returns 404
     */
    @Test
    public void testUserAccessDialog_GetPermissions_PhotoNotFound() throws Exception {
        mockMvc.perform(get("/api/photos/99999/permissions"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test Case 7: Update permissions for non-existent photo returns 404
     */
    @Test
    public void testUserAccessDialog_UpdatePermissions_PhotoNotFound() throws Exception {
        List<Long> permissions = Arrays.asList(user1.getUserId());

        mockMvc.perform(put("/api/photos/99999/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isNotFound());
    }
}
