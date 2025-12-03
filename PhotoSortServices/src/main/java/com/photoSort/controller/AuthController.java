/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.model.User;
import com.photoSort.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller for checking auth status
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current authenticated user
     * @param principal OAuth2User principal
     * @return Current user data or 401 if not authenticated
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("email", user.getEmail());
        response.put("displayName", user.getDisplayName());
        response.put("userType", user.getUserType().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Spring Security handles the actual logout
        // This endpoint is just for the frontend to call
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
