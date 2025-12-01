/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for advanced search filter criteria.
 * Represents a single filter row in the advanced search UI.
 * Multiple filters are combined with AND logic.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilterDTO {

    /**
     * Column to filter on: displayName, email, userType, firstLoginDate, lastLoginDate
     */
    private String column;

    /**
     * Value to search for
     */
    private String value;

    /**
     * Filter operation: CONTAINS or NOT_CONTAINS
     */
    private FilterOperation operation;

    /**
     * Filter operation enum
     */
    public enum FilterOperation {
        CONTAINS,
        NOT_CONTAINS
    }
}
