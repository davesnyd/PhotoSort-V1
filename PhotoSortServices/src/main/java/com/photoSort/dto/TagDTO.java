/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Tag information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    private Long tagId;
    private String tagValue;
}
