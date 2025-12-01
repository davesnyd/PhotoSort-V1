/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.dto.PagedResponse;
import com.photoSort.dto.SearchFilterDTO;
import com.photoSort.dto.UserDTO;
import com.photoSort.dto.UserUpdateRequest;
import com.photoSort.model.User;
import com.photoSort.service.CustomOAuth2UserService;
import com.photoSort.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user management endpoints.
 * Provides endpoints for listing users with search/pagination and updating user types.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

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
}
