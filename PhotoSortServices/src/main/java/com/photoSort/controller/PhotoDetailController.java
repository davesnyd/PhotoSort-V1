/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.*;
import com.photoSort.model.*;
import com.photoSort.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for photo detail operations (Step 10: Image Display Page)
 * Handles photo details retrieval, image serving, and metadata/tag editing
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoDetailController {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ExifDataRepository exifDataRepository;

    @Autowired
    private PhotoMetadataRepository photoMetadataRepository;

    @Autowired
    private PhotoTagRepository photoTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MetadataFieldRepository metadataFieldRepository;

    @Autowired
    private com.photoSort.service.PhotoProcessingService photoProcessingService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get complete photo details including EXIF, metadata, and tags
     *
     * @param id Photo ID
     * @return Photo details with all related data
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PhotoDetailDTO>> getPhotoDetails(@PathVariable Long id) {
        try {
            // Fetch photo
            Photo photo = photoRepository.findById(id).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("PHOTO_NOT_FOUND", "Photo not found"));
            }

            // Build DTO
            PhotoDetailDTO dto = new PhotoDetailDTO();
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

            // Owner information
            if (photo.getOwner() != null) {
                dto.setOwnerId(photo.getOwner().getUserId());
                dto.setOwnerEmail(photo.getOwner().getEmail());
                dto.setOwnerDisplayName(photo.getOwner().getDisplayName());
            }

            // EXIF data
            ExifData exifData = exifDataRepository.findByPhoto(photo).orElse(null);
            if (exifData != null) {
                ExifDataDTO exifDTO = new ExifDataDTO();
                exifDTO.setExifId(exifData.getExifId());
                exifDTO.setDateTimeOriginal(exifData.getDateTimeOriginal());
                exifDTO.setCameraMake(exifData.getCameraMake());
                exifDTO.setCameraModel(exifData.getCameraModel());
                exifDTO.setGpsLatitude(exifData.getGpsLatitude());
                exifDTO.setGpsLongitude(exifData.getGpsLongitude());
                exifDTO.setExposureTime(exifData.getExposureTime());
                exifDTO.setFNumber(exifData.getFNumber());
                exifDTO.setIsoSpeed(exifData.getIsoSpeed());
                exifDTO.setFocalLength(exifData.getFocalLength());
                exifDTO.setOrientation(exifData.getOrientation());
                dto.setExifData(exifDTO);
            }

            // Custom metadata
            List<PhotoMetadata> metadataList = photoMetadataRepository.findByPhoto(photo);
            List<MetadataDTO> metadataDTOs = metadataList.stream()
                    .map(pm -> new MetadataDTO(
                            pm.getMetadataId(),
                            pm.getField().getFieldName(),
                            pm.getMetadataValue()))
                    .collect(Collectors.toList());
            dto.setMetadata(metadataDTOs);

            // Tags
            List<PhotoTag> photoTags = photoTagRepository.findByPhoto(photo);
            List<TagDTO> tagDTOs = photoTags.stream()
                    .map(pt -> new TagDTO(
                            pt.getTag().getTagId(),
                            pt.getTag().getTagValue()))
                    .collect(Collectors.toList());
            dto.setTags(tagDTOs);

            return ResponseEntity.ok(ApiResponse.success(dto));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                            "Error retrieving photo details: " + e.getMessage()));
        }
    }

    /**
     * Serve photo image file from disk
     *
     * @param id Photo ID
     * @return Image file as stream
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getPhotoImage(@PathVariable Long id) {
        try {
            // Fetch photo
            Photo photo = photoRepository.findById(id).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404).build();
            }

            // Read file from disk
            File imageFile = new File(photo.getFilePath());
            if (!imageFile.exists()) {
                return ResponseEntity.status(404).build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(imageFile));

            // Determine content type from file extension
            String contentType = "image/jpeg"; // Default
            String fileName = photo.getFileName().toLowerCase();
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.endsWith(".bmp")) {
                contentType = "image/bmp";
            } else if (fileName.endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(imageFile.length())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo.getFileName() + "\"")
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Serve photo thumbnail from disk
     *
     * @param id Photo ID
     * @return Thumbnail image file as stream
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getPhotoThumbnail(@PathVariable Long id) {
        try {
            // Fetch photo
            Photo photo = photoRepository.findById(id).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404).build();
            }

            // Check if thumbnail exists
            if (photo.getThumbnailPath() == null || photo.getThumbnailPath().isEmpty()) {
                return ResponseEntity.status(404).build();
            }

            // Read thumbnail file from disk
            File thumbnailFile = new File(photo.getThumbnailPath());
            if (!thumbnailFile.exists()) {
                return ResponseEntity.status(404).build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(thumbnailFile));

            // Determine content type from file extension
            String contentType = "image/jpeg"; // Default
            String fileName = thumbnailFile.getName().toLowerCase();
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.endsWith(".bmp")) {
                contentType = "image/bmp";
            } else if (fileName.endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(thumbnailFile.length())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + thumbnailFile.getName() + "\"")
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Update photo custom metadata
     * Replaces all existing metadata with the provided list
     *
     * @param id Photo ID
     * @param metadataList List of metadata fields and values
     * @return Success message
     */
    @PutMapping("/{id}/metadata")
    @Transactional
    public ResponseEntity<ApiResponse<String>> updatePhotoMetadata(
            @PathVariable Long id,
            @RequestBody List<MetadataDTO> metadataList) {

        try {
            // Verify photo exists
            Photo photo = photoRepository.findById(id).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("PHOTO_NOT_FOUND", "Photo not found"));
            }

            // Remove all existing metadata for this photo
            List<PhotoMetadata> existingMetadata = photoMetadataRepository.findByPhoto(photo);
            photoMetadataRepository.deleteAll(existingMetadata);

            // Flush to ensure deletes are committed before inserts
            entityManager.flush();

            // Create new metadata
            for (MetadataDTO dto : metadataList) {
                // Find or create metadata field
                MetadataField field = metadataFieldRepository.findByFieldName(dto.getFieldName())
                        .orElseGet(() -> {
                            MetadataField newField = new MetadataField();
                            newField.setFieldName(dto.getFieldName());
                            return metadataFieldRepository.save(newField);
                        });

                // Create photo metadata
                PhotoMetadata photoMetadata = new PhotoMetadata();
                photoMetadata.setPhoto(photo);
                photoMetadata.setField(field);
                photoMetadata.setMetadataValue(dto.getMetadataValue());
                photoMetadataRepository.save(photoMetadata);
            }

            return ResponseEntity.ok(ApiResponse.success("Metadata updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                            "Error updating metadata: " + e.getMessage()));
        }
    }

    /**
     * Update photo tags
     * Replaces all existing tags with the provided list
     *
     * @param id Photo ID
     * @param tagValues List of tag values
     * @return Success message
     */
    @PutMapping("/{id}/tags")
    @Transactional
    public ResponseEntity<ApiResponse<String>> updatePhotoTags(
            @PathVariable Long id,
            @RequestBody List<String> tagValues) {

        try {
            // Verify photo exists
            Photo photo = photoRepository.findById(id).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("PHOTO_NOT_FOUND", "Photo not found"));
            }

            // Remove all existing photo-tag associations
            List<PhotoTag> existingPhotoTags = photoTagRepository.findByPhoto(photo);
            photoTagRepository.deleteAll(existingPhotoTags);

            // Flush to ensure deletes are committed before inserts
            entityManager.flush();

            // Create new tag associations
            for (String tagValue : tagValues) {
                // Find or create tag
                Tag tag = tagRepository.findByTagValue(tagValue)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setTagValue(tagValue);
                            return tagRepository.save(newTag);
                        });

                // Create photo-tag association
                PhotoTag photoTag = new PhotoTag();
                photoTag.setPhoto(photo);
                photoTag.setTag(tag);
                photoTagRepository.save(photoTag);
            }

            return ResponseEntity.ok(ApiResponse.success("Tags updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                            "Error updating tags: " + e.getMessage()));
        }
    }

    /**
     * Reprocess a photo through the complete processing pipeline
     * Regenerates thumbnails, re-extracts EXIF, metadata, and tags
     *
     * @param id Photo ID
     * @return Success message
     */
    @PostMapping("/{id}/reprocess")
    @Transactional
    public ResponseEntity<ApiResponse<String>> reprocessPhoto(@PathVariable Long id) {
        try {
            // Verify photo exists
            Photo photo = photoRepository.findById(id).orElse(null);
            if (photo == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("PHOTO_NOT_FOUND", "Photo not found"));
            }

            // Get the file path
            String filePath = photo.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("INVALID_FILE_PATH", "Photo has no file path"));
            }

            File photoFile = new File(filePath);
            if (!photoFile.exists()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("FILE_NOT_FOUND", "Photo file not found on disk: " + filePath));
            }

            // Get owner email for processing (use existing owner)
            String ownerEmail = photo.getOwner() != null ? photo.getOwner().getEmail() : null;

            // Reprocess the photo
            photoProcessingService.processPhoto(photoFile, ownerEmail);

            return ResponseEntity.ok(ApiResponse.success("Photo reprocessed successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                            "Error reprocessing photo: " + e.getMessage()));
        }
    }
}
