/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for updating user information.
 * Currently supports updating user type only.
 *
 * Validation is performed in the controller/service layer:
 * - userType must not be null
 * - userType must be either "USER" or "ADMIN"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String userType;
}
