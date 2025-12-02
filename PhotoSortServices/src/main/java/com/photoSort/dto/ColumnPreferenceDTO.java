/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import com.photoSort.model.UserColumnPreference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User Column Preferences.
 * Used to transfer column preference data between backend and frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnPreferenceDTO {

    private UserColumnPreference.ColumnType columnType;
    private String columnName;
    private Integer displayOrder;

    /**
     * Create ColumnPreferenceDTO from UserColumnPreference entity.
     *
     * @param preference The user column preference entity
     * @return ColumnPreferenceDTO
     */
    public static ColumnPreferenceDTO fromEntity(UserColumnPreference preference) {
        return new ColumnPreferenceDTO(
                preference.getColumnType(),
                preference.getColumnName(),
                preference.getDisplayOrder()
        );
    }
}
