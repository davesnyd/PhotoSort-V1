/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.photoSort.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom OAuth2 User Service that integrates Google authentication
 * with PhotoSort user management.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserService userService;

    @Autowired
    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Load user from OAuth2 provider and create/update user in database.
     *
     * @param userRequest OAuth2 user request
     * @return OAuth2User with additional attributes
     * @throws OAuth2AuthenticationException if authentication fails
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("OAuth2 loadUser called - processing OAuth login");

        // Get user info from Google
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extract user attributes
        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        logger.info("OAuth2 user info received - googleId: {}, email: {}, name: {}", googleId, email, name);

        // Process OAuth login (create or update user)
        User user = userService.processOAuthLogin(googleId, email, name);

        logger.info("OAuth2 user processed - userId: {}, userType: {}", user.getUserId(), user.getUserType());

        // Create custom OAuth2User with our user ID
        return new CustomOAuth2User(oauth2User, user);
    }

    /**
     * Custom OAuth2User implementation that includes PhotoSort user information.
     */
    public static class CustomOAuth2User implements OAuth2User {
        private final OAuth2User oauth2User;
        private final User user;

        public CustomOAuth2User(OAuth2User oauth2User, User user) {
            this.oauth2User = oauth2User;
            this.user = user;
        }

        @Override
        public Map<String, Object> getAttributes() {
            Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
            attributes.put("userId", user.getUserId());
            attributes.put("userType", user.getUserType().toString());
            return attributes;
        }

        @Override
        public String getName() {
            return oauth2User.getName();
        }

        /**
         * Get the PhotoSort user associated with this OAuth2 user.
         *
         * @return PhotoSort User entity
         */
        public User getUser() {
            return user;
        }

        /**
         * Get the user ID.
         *
         * @return User ID
         */
        public Long getUserId() {
            return user.getUserId();
        }

        /**
         * Check if this user is an administrator.
         *
         * @return true if admin, false otherwise
         */
        public boolean isAdmin() {
            return user.getUserType() == User.UserType.ADMIN;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return oauth2User.getAuthorities();
        }
    }
}
