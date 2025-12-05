/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.jpeg.JpegDirectory;
import com.photoSort.model.*;
import com.photoSort.repository.*;
import com.photoSort.model.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Service for orchestrating complete photo processing pipeline (Step 18)
 * Coordinates EXIF extraction, metadata parsing, STAG tagging, and database persistence
 */
@Service
public class PhotoProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoProcessingService.class);

    @Autowired
    private ExifDataService exifDataService;

    @Autowired
    private MetadataParserService metadataParserService;

    @Autowired
    private StagService stagService;

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ExifDataRepository exifDataRepository;

    @Autowired
    private PhotoMetadataRepository photoMetadataRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PhotoTagRepository photoTagRepository;

    @Autowired
    private MetadataFieldRepository metadataFieldRepository;

    @Autowired
    private ScriptExecutionLogRepository scriptExecutionLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private ThumbnailService thumbnailService;

    /**
     * Process a photo through the complete pipeline
     *
     * @param photoFile The photo file to process
     * @param commitAuthor The Git commit author email (photo owner)
     * @return The created or updated Photo record
     */
    @Transactional
    public Photo processPhoto(File photoFile, String commitAuthor) {
        logger.info("Processing photo: {}", photoFile.getName());

        try {
            // Step 1: Determine owner
            User owner = findOrCreateUser(commitAuthor);

            // Step 2: Check for duplicate (by file_path)
            String filePath = photoFile.getAbsolutePath();
            Photo photo = photoRepository.findByFilePath(filePath).orElse(null);
            boolean isUpdate = (photo != null);

            if (photo == null) {
                photo = new Photo();
                photo.setFilePath(filePath);
            }

            // Step 3: Extract file metadata
            photo.setFileName(photoFile.getName());
            photo.setFileSize(photoFile.length());
            photo.setOwner(owner);

            BasicFileAttributes attrs = Files.readAttributes(photoFile.toPath(), BasicFileAttributes.class);
            photo.setFileCreatedDate(LocalDateTime.ofInstant(
                attrs.creationTime().toInstant(),
                ZoneId.systemDefault()
            ));
            photo.setFileModifiedDate(LocalDateTime.ofInstant(
                attrs.lastModifiedTime().toInstant(),
                ZoneId.systemDefault()
            ));

            // Step 4: Extract image dimensions
            Integer[] dimensions = extractImageDimensions(photoFile);
            if (dimensions != null) {
                photo.setImageWidth(dimensions[0]);
                photo.setImageHeight(dimensions[1]);
            }

            // Step 5: Generate thumbnail
            String thumbnailPath = thumbnailService.generateThumbnail(photoFile);
            photo.setThumbnailPath(thumbnailPath);

            // Step 6: Save Photo record (must save before creating associations)
            photo = photoRepository.save(photo);
            logger.debug("{} photo record: {} (ID: {})",
                isUpdate ? "Updated" : "Created", photo.getFileName(), photo.getPhotoId());

            // Step 7: Extract and save EXIF data
            processExifData(photoFile, photo);

            // Step 8: Parse and save metadata file
            processMetadataFile(photoFile, photo);

            // Step 9: Generate and save STAG tags
            processStagTags(photoFile, photo);

            // Step 10: Execute custom scripts (placeholder for Step 19)
            processCustomScripts(photoFile, photo);

            logger.info("Successfully processed photo: {} (ID: {})", photo.getFileName(), photo.getPhotoId());
            return photo;

        } catch (Exception e) {
            logger.error("Error processing photo {}: {}", photoFile.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to process photo: " + photoFile.getName(), e);
        }
    }

    /**
     * Extract and save EXIF data
     */
    private void processExifData(File photoFile, Photo photo) {
        try {
            ExifData exifData = exifDataService.extractExifData(photoFile);

            if (exifData != null && hasExifData(exifData)) {
                // Check if EXIF data already exists for this photo
                ExifData existingExif = exifDataRepository.findByPhoto(photo).orElse(null);

                if (existingExif != null) {
                    // Update existing EXIF data
                    copyExifData(exifData, existingExif);
                    exifDataRepository.save(existingExif);
                    logger.debug("Updated EXIF data for photo: {}", photo.getFileName());
                } else {
                    // Create new EXIF data
                    exifData.setPhoto(photo);
                    exifDataRepository.save(exifData);
                    logger.debug("Saved EXIF data for photo: {}", photo.getFileName());
                }
            } else {
                logger.debug("No EXIF data found for photo: {}", photo.getFileName());
            }
        } catch (Exception e) {
            logger.warn("Failed to process EXIF data for {}: {}", photoFile.getName(), e.getMessage());
            // Continue processing - EXIF data is optional
        }
    }

    /**
     * Check if ExifData has any meaningful data
     */
    private boolean hasExifData(ExifData exifData) {
        return exifData.getCameraMake() != null ||
               exifData.getCameraModel() != null ||
               exifData.getGpsLatitude() != null ||
               exifData.getGpsLongitude() != null ||
               exifData.getDateTimeOriginal() != null;
    }

    /**
     * Copy EXIF data fields from source to destination
     */
    private void copyExifData(ExifData source, ExifData destination) {
        destination.setCameraMake(source.getCameraMake());
        destination.setCameraModel(source.getCameraModel());
        destination.setFocalLength(source.getFocalLength());
        destination.setFNumber(source.getFNumber());
        destination.setExposureTime(source.getExposureTime());
        destination.setIsoSpeed(source.getIsoSpeed());
        destination.setOrientation(source.getOrientation());
        destination.setDateTimeOriginal(source.getDateTimeOriginal());
        destination.setGpsLatitude(source.getGpsLatitude());
        destination.setGpsLongitude(source.getGpsLongitude());
    }

    /**
     * Parse and save metadata from .metadata file
     */
    private void processMetadataFile(File photoFile, Photo photo) {
        try {
            // Check for .metadata file
            File metadataFile = new File(photoFile.getAbsolutePath() + ".metadata");

            if (metadataFile.exists()) {
                Map<String, Object> metadata = metadataParserService.parseMetadataFile(metadataFile);

                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    String fieldName = entry.getKey();
                    Object value = entry.getValue();

                    // Special handling for tags field (processed separately below)
                    if ("tags".equals(fieldName) && value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> tags = (List<String>) value;
                        processMetadataFileTags(photo, tags);
                    } else {
                        // Regular metadata field
                        String stringValue = (value != null) ? value.toString() : "";
                        saveMetadataField(photo, fieldName, stringValue);
                    }
                }

                logger.debug("Processed metadata file for photo: {}", photo.getFileName());
            } else {
                logger.debug("No metadata file found for photo: {}", photo.getFileName());
            }
        } catch (Exception e) {
            logger.warn("Failed to process metadata file for {}: {}", photoFile.getName(), e.getMessage());
            // Continue processing - metadata file is optional
        }
    }

    /**
     * Save a metadata field value
     */
    private void saveMetadataField(Photo photo, String fieldName, String value) {
        MetadataField field = findOrCreateMetadataField(fieldName);

        // Check if metadata record already exists
        PhotoMetadata existing = photoMetadataRepository.findByPhotoAndField(photo, field).orElse(null);

        if (existing != null) {
            existing.setMetadataValue(value);
            photoMetadataRepository.save(existing);
        } else {
            PhotoMetadata photoMetadata = new PhotoMetadata();
            photoMetadata.setPhoto(photo);
            photoMetadata.setField(field);
            photoMetadata.setMetadataValue(value);
            photoMetadataRepository.save(photoMetadata);
        }
    }

    /**
     * Process tags from metadata file
     */
    private void processMetadataFileTags(Photo photo, List<String> tags) {
        for (String tagValue : tags) {
            if (tagValue != null && !tagValue.trim().isEmpty()) {
                Tag tag = findOrCreateTag(tagValue.trim());
                createPhotoTagAssociation(photo, tag);
            }
        }

        if (!tags.isEmpty()) {
            logger.debug("Saved {} tag(s) from metadata file for photo: {}", tags.size(), photo.getFileName());
        }
    }

    /**
     * Generate and save STAG tags
     */
    private void processStagTags(File photoFile, Photo photo) {
        try {
            List<String> stagTags = stagService.generateTags(photoFile);

            if (stagTags != null && !stagTags.isEmpty()) {
                for (String tagValue : stagTags) {
                    if (tagValue != null && !tagValue.trim().isEmpty()) {
                        Tag tag = findOrCreateTag(tagValue.trim());
                        createPhotoTagAssociation(photo, tag);
                    }
                }

                logger.debug("Saved {} STAG tag(s) for photo: {}", stagTags.size(), photo.getFileName());

                // Log STAG script execution
                logScriptExecution(photo, "STAG", ScriptExecutionLog.ExecutionStatus.SUCCESS, null);
            }
        } catch (Exception e) {
            logger.warn("Failed to generate STAG tags for {}: {}", photoFile.getName(), e.getMessage());
            logScriptExecution(photo, "STAG", ScriptExecutionLog.ExecutionStatus.FAILURE, e.getMessage());
            // Continue processing - STAG tags are optional
        }
    }

    /**
     * Execute custom scripts based on file extension (Step 19)
     */
    private void processCustomScripts(File photoFile, Photo photo) {
        try {
            // Get file extension
            String fileName = photoFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex == -1) {
                logger.debug("File has no extension, skipping custom script execution");
                return;
            }

            String extension = fileName.substring(dotIndex); // Includes the dot

            // Get script for extension
            Script script = scriptExecutionService.getScriptForExtension(extension);

            if (script != null) {
                logger.debug("Executing custom script '{}' for file: {}",
                    script.getScriptName(), photoFile.getName());
                scriptExecutionService.executeScript(script, photoFile, photo);
            } else {
                logger.debug("No custom script configured for extension: {}", extension);
            }

        } catch (Exception e) {
            logger.warn("Failed to execute custom script for {}: {}", photoFile.getName(), e.getMessage());
            // Continue processing - custom scripts are optional
        }
    }

    /**
     * Create photo-tag association if it doesn't exist
     */
    private void createPhotoTagAssociation(Photo photo, Tag tag) {
        // Check if association already exists
        boolean exists = photoTagRepository.findByPhotoAndTag(photo, tag).isPresent();

        if (!exists) {
            PhotoTag photoTag = new PhotoTag();
            photoTag.setPhoto(photo);
            photoTag.setTag(tag);
            photoTagRepository.save(photoTag);
        }
    }

    /**
     * Log script execution to script_execution_log
     */
    private void logScriptExecution(Photo photo, String scriptName, ScriptExecutionLog.ExecutionStatus status, String errorMessage) {
        try {
            // Find script by name (STAG script may not be in database yet)
            Script script = scriptRepository.findByScriptName(scriptName).orElse(null);

            ScriptExecutionLog log = new ScriptExecutionLog();
            log.setScript(script);  // May be null for STAG
            log.setPhoto(photo);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);

            scriptExecutionLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to log script execution for {}: {}", scriptName, e.getMessage());
            // Continue - logging failure should not stop processing
        }
    }

    /**
     * Extract image dimensions
     */
    private Integer[] extractImageDimensions(File photoFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(photoFile);
            JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

            if (jpegDirectory != null) {
                Integer width = jpegDirectory.getImageWidth();
                Integer height = jpegDirectory.getImageHeight();

                if (width != null && height != null) {
                    return new Integer[]{width, height};
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract image dimensions from {}: {}", photoFile.getName(), e.getMessage());
        }

        return null;
    }

    /**
     * Find or create user by email
     */
    private User findOrCreateUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            return getDefaultAdmin();
        }

        return userRepository.findByEmail(email).orElseGet(this::getDefaultAdmin);
    }

    /**
     * Get default admin user
     */
    private User getDefaultAdmin() {
        return userRepository.findAll().stream()
            .filter(u -> u.getUserType() == User.UserType.ADMIN)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No admin user found in system"));
    }

    /**
     * Find or create tag
     */
    private Tag findOrCreateTag(String tagValue) {
        return tagRepository.findByTagValue(tagValue).orElseGet(() -> {
            Tag tag = new Tag();
            tag.setTagValue(tagValue);
            return tagRepository.save(tag);
        });
    }

    /**
     * Find or create metadata field
     */
    private MetadataField findOrCreateMetadataField(String fieldName) {
        return metadataFieldRepository.findByFieldName(fieldName).orElseGet(() -> {
            MetadataField field = new MetadataField();
            field.setFieldName(fieldName);
            return metadataFieldRepository.save(field);
        });
    }
}
