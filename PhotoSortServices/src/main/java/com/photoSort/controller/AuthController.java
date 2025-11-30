/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.service.CustomOAuth2UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for authentication-related endpoints.
 * Handles user authentication status, logout, and user information.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Get current authenticated user information.
     *
     * @param principal OAuth2 user principal (injected by Spring Security)
     * @return User information or 401 if not authenticated
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Map<String, Object> userData = new HashMap<>();

        if (principal instanceof CustomOAuth2UserService.CustomOAuth2User customUser) {
            userData.put("userId", customUser.getUserId());
            userData.put("email", customUser.getUser().getEmail());
            userData.put("displayName", customUser.getUser().getDisplayName());
            userData.put("userType", customUser.getUser().getUserType().toString());
            userData.put("isAdmin", customUser.isAdmin());
        } else {
            userData.put("name", principal.getName());
            userData.put("email", principal.getAttribute("email"));
        }

        response.put("data", userData);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user is authenticated.
     *
     * @param principal OAuth2 user principal
     * @return Authentication status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(
            @AuthenticationPrincipal OAuth2User principal) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", principal != null);

        if (principal instanceof CustomOAuth2UserService.CustomOAuth2User customUser) {
            data.put("isAdmin", customUser.isAdmin());
        }

        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
