/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import com.photoSort.model.Photo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Photo entity.
 * Used to transfer photo data between backend and frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO {

    private Long photoId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime fileCreatedDate;
    private LocalDateTime fileModifiedDate;
    private LocalDateTime addedToSystemDate;
    private Boolean isPublic;
    private String ownerDisplayName;
    private Long ownerId;
    private Integer imageWidth;
    private Integer imageHeight;
    private String thumbnailPath;

    /**
     * Create PhotoDTO from Photo entity.
     *
     * @param photo The photo entity
     * @return PhotoDTO
     */
    public static PhotoDTO fromEntity(Photo photo) {
        PhotoDTO dto = new PhotoDTO();
        dto.setPhotoId(photo.getPhotoId());
        dto.setFileName(photo.getFileName());
        dto.setFilePath(photo.getFilePath());
        dto.setFileSize(photo.getFileSize());
        dto.setFileCreatedDate(photo.getFileCreatedDate());
        dto.setFileModifiedDate(photo.getFileModifiedDate());
        dto.setAddedToSystemDate(photo.getAddedToSystemDate());
        dto.setIsPublic(photo.getIsPublic());
        dto.setImageWidth(photo.getImageWidth());
        dto.setImageHeight(photo.getImageHeight());
        dto.setThumbnailPath(photo.getThumbnailPath());

        if (photo.getOwner() != null) {
            dto.setOwnerDisplayName(photo.getOwner().getDisplayName());
            dto.setOwnerId(photo.getOwner().getUserId());
        }

        return dto;
    }
}
