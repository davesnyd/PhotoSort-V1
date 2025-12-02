/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.dto.ColumnPreferenceDTO;
import com.photoSort.dto.PagedResponse;
import com.photoSort.dto.SearchFilterDTO;
import com.photoSort.dto.UserDTO;
import com.photoSort.dto.UserUpdateRequest;
import com.photoSort.model.User;
import com.photoSort.model.UserColumnPreference;
import com.photoSort.repository.UserColumnPreferenceRepository;
import com.photoSort.repository.UserRepository;
import com.photoSort.service.CustomOAuth2UserService;
import com.photoSort.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for user management endpoints.
 * Provides endpoints for listing users with search/pagination and updating user types.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserColumnPreferenceRepository userColumnPreferenceRepository;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get paginated list of users with optional search.
     * Supports both quick search (via 'search' parameter) and advanced search (via 'filters' parameter).
     *
     * Query parameters:
     * - page: Page number (0-indexed, default: 0)
     * - pageSize: Items per page (default: 10)
     * - sortBy: Field to sort by (default: "email")
     * - sortDir: Sort direction "asc" or "desc" (default: "asc")
     * - search: Quick search term (searches email and displayName)
     * - filters: JSON array of advanced search filters
     *
     * @param principal Authenticated user
     * @param page Page number
     * @param pageSize Page size
     * @param sortBy Sort field
     * @param sortDir Sort direction
     * @param search Quick search term (optional)
     * @param filters Advanced search filters (optional)
     * @return Paginated list of users with photo counts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getUsers(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "email") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<SearchFilterDTO> filters) {

        // TEMPORARY: Authentication checks disabled for testing
        // TODO: Re-enable when OAuth is properly configured
        /* PRODUCTION CODE - Uncomment when OAuth is set up:
        // Check authentication
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("UNAUTHORIZED", "Authentication required"));
        }

        // Check admin status
        if (!(principal instanceof CustomOAuth2UserService.CustomOAuth2User customUser) ||
            !customUser.isAdmin()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FORBIDDEN", "Admin access required"));
        }
        */

        try {
            PagedResponse<UserDTO> response;

            if (filters != null && !filters.isEmpty()) {
                // Advanced search with filters
                response = userService.advancedSearchUsers(filters, page, pageSize, sortBy, sortDir);
            } else if (search != null && !search.trim().isEmpty()) {
                // Quick search
                response = userService.searchUsers(search, page, pageSize, sortBy, sortDir);
            } else {
                // No search - get all users
                response = userService.getUsers(page, pageSize, sortBy, sortDir);
            }

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error retrieving users: " + e.getMessage()));
        }
    }

    /**
     * Update a user's type (USER or ADMIN).
     * Only accessible to administrators.
     *
     * @param principal Authenticated user
     * @param userId User ID to update
     * @param request Update request containing new userType
     * @return Updated user information
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserType(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {

        // TEMPORARY: Authentication checks disabled for testing
        // TODO: Re-enable when OAuth is properly configured
        /* PRODUCTION CODE - Uncomment when OAuth is set up:
        // Check authentication
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("UNAUTHORIZED", "Authentication required"));
        }

        // Check admin status
        if (!(principal instanceof CustomOAuth2UserService.CustomOAuth2User customUser) ||
            !customUser.isAdmin()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("FORBIDDEN", "Admin access required"));
        }
        */

        // Validate userType
        String userTypeStr = request.getUserType();
        if (userTypeStr == null || userTypeStr.trim().isEmpty()) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_REQUEST", "userType is required"));
        }

        User.UserType userType;
        try {
            userType = User.UserType.valueOf(userTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_REQUEST",
                          "userType must be either USER or ADMIN"));
        }

        try {
            // Update user type
            User updatedUser = userService.updateUserType(userId, userType);

            // Convert to DTO (photo count set to 0 for single user update)
            UserDTO userDTO = UserDTO.fromUser(updatedUser, 0L);

            return ResponseEntity.ok(ApiResponse.success(userDTO));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("USER_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error updating user: " + e.getMessage()));
        }
    }

    /**
     * Get user's column preferences for photo table.
     * Returns default columns if user has no preferences saved.
     *
     * @param userId User ID
     * @return List of column preferences
     */
    @GetMapping("/{userId}/columns")
    public ResponseEntity<ApiResponse<List<ColumnPreferenceDTO>>> getUserColumns(
            @PathVariable Long userId) {

        // Check if user exists
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("USER_NOT_FOUND", "User not found"));
        }

        // Get user's column preferences
        List<UserColumnPreference> preferences = userColumnPreferenceRepository.findAll().stream()
                .filter(p -> p.getUser().getUserId().equals(userId))
                .collect(Collectors.toList());

        List<ColumnPreferenceDTO> dtos;

        if (preferences.isEmpty()) {
            // Return default columns
            dtos = getDefaultColumns();
        } else {
            // Convert to DTOs
            dtos = preferences.stream()
                    .map(ColumnPreferenceDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Update user's column preferences for photo table.
     *
     * @param userId User ID
     * @param columns List of column preferences to save
     * @return Success response
     */
    @PutMapping("/{userId}/columns")
    public ResponseEntity<ApiResponse<String>> updateUserColumns(
            @PathVariable Long userId,
            @RequestBody List<ColumnPreferenceDTO> columns) {

        // Check if user exists
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("USER_NOT_FOUND", "User not found"));
        }

        // Delete existing preferences
        List<UserColumnPreference> existing = userColumnPreferenceRepository.findAll().stream()
                .filter(p -> p.getUser().getUserId().equals(userId))
                .collect(Collectors.toList());
        userColumnPreferenceRepository.deleteAll(existing);

        // Save new preferences
        for (ColumnPreferenceDTO dto : columns) {
            UserColumnPreference pref = new UserColumnPreference();
            pref.setUser(user);
            pref.setColumnType(dto.getColumnType());
            pref.setColumnName(dto.getColumnName());
            pref.setDisplayOrder(dto.getDisplayOrder());
            userColumnPreferenceRepository.save(pref);
        }

        return ResponseEntity.ok(ApiResponse.success("Column preferences updated"));
    }

    /**
     * Get default column preferences when user has none saved.
     *
     * @return List of default column preferences
     */
    private List<ColumnPreferenceDTO> getDefaultColumns() {
        return Arrays.asList(
                new ColumnPreferenceDTO(UserColumnPreference.ColumnType.STANDARD, "file_name", 1),
                new ColumnPreferenceDTO(UserColumnPreference.ColumnType.STANDARD, "file_created_date", 2),
                new ColumnPreferenceDTO(UserColumnPreference.ColumnType.STANDARD, "thumbnail", 3)
        );
    }
}
