/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import com.photoSort.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User information.
 * Excludes sensitive fields like googleId and includes calculated fields like photoCount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long userId;
    private String email;
    private String displayName;
    private String userType;
    private LocalDateTime firstLoginDate;
    private LocalDateTime lastLoginDate;
    private Long photoCount;

    /**
     * Factory method to create UserDTO from User entity with photo count.
     *
     * @param user User entity
     * @param photoCount Number of photos owned by user
     * @return UserDTO
     */
    public static UserDTO fromUser(User user, long photoCount) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setUserType(user.getUserType().toString());
        dto.setFirstLoginDate(user.getFirstLoginDate());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setPhotoCount(photoCount);
        return dto;
    }
}
