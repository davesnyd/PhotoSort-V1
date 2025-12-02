/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.dto.PagedResponse;
import com.photoSort.dto.PhotoDTO;
import com.photoSort.model.User;
import com.photoSort.repository.UserRepository;
import com.photoSort.service.CustomOAuth2UserService;
import com.photoSort.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for photo management endpoints.
 * Provides endpoints for listing photos with permission-based filtering.
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get paginated list of photos with permission filtering.
     *
     * For regular users: Returns photos they own, public photos, and photos they have permission for
     * For admins: Returns all photos (optionally filtered by userId)
     *
     * Query parameters:
     * - page: Page number (0-indexed, default: 0)
     * - size: Items per page (default: 10)
     * - sort: Field to sort by (default: "fileName")
     * - direction: Sort direction "asc" or "desc" (default: "asc")
     * - search: Quick search term (searches fileName and filePath)
     * - userId: Filter by owner user ID (admin only)
     * - filterField1, filterValue1, filterType1: Advanced filter 1
     * - filterField2, filterValue2, filterType2: Advanced filter 2
     *
     * @param principal Authenticated user
     * @param page Page number
     * @param size Page size
     * @param sort Sort field
     * @param direction Sort direction
     * @param search Quick search term (optional)
     * @param userId User ID to filter by (admin only, optional)
     * @param filterField1 Advanced filter field 1
     * @param filterValue1 Advanced filter value 1
     * @param filterType1 Advanced filter type 1 (MUST_CONTAIN or MUST_NOT_CONTAIN)
     * @param filterField2 Advanced filter field 2
     * @param filterValue2 Advanced filter value 2
     * @param filterType2 Advanced filter type 2
     * @return Paginated list of photos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PhotoDTO>>> getPhotos(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fileName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String filterField1,
            @RequestParam(required = false) String filterValue1,
            @RequestParam(required = false) String filterType1,
            @RequestParam(required = false) String filterField2,
            @RequestParam(required = false) String filterValue2,
            @RequestParam(required = false) String filterType2) {

        // Get authentication from security context (works with both OAuth2 and @WithMockUser)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;

        if (authentication != null) {
            if (authentication.getPrincipal() instanceof OAuth2User) {
                // OAuth2 authentication
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                userEmail = oauth2User.getAttribute("email");
            } else {
                // @WithMockUser or other authentication - username is the email
                userEmail = authentication.getName();
            }
        }

        if (userEmail == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("UNAUTHORIZED", "Authentication required"));
        }

        User currentUser = userRepository.findByEmail(userEmail)
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("UNAUTHORIZED", "User not found"));
        }

        // Create pageable with sorting
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<PhotoDTO> photoPage;

        // Check if user is admin
        boolean isAdmin = currentUser.getUserType() == User.UserType.ADMIN;

        if (isAdmin) {
            // Admin: Get all photos (optionally filtered by userId)
            photoPage = photoService.getPhotosForAdmin(
                    userId, pageable, search,
                    filterField1, filterValue1, filterType1,
                    filterField2, filterValue2, filterType2
            );
        } else {
            // Regular user: Get photos with permission filtering
            photoPage = photoService.getPhotosForUser(
                    currentUser, pageable, search,
                    filterField1, filterValue1, filterType1,
                    filterField2, filterValue2, filterType2
            );
        }

        PagedResponse<PhotoDTO> response = new PagedResponse<>(
                photoPage.getContent(),
                photoPage.getNumber(),
                photoPage.getSize(),
                photoPage.getTotalPages(),
                photoPage.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
