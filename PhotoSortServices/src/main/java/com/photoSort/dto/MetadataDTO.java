/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for custom metadata field and value
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataDTO {
    private Long metadataId;
    private String fieldName;
    private String metadataValue;
}
