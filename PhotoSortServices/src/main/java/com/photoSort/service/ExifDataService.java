/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.photoSort.model.ExifData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Service for extracting EXIF data from photo files (Step 15)
 * Uses metadata-extractor library to read EXIF metadata
 */
@Service
public class ExifDataService {

    private static final Logger logger = LoggerFactory.getLogger(ExifDataService.class);

    // Date/time formats commonly found in EXIF data
    private static final DateTimeFormatter[] EXIF_DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss.SSS")
    };

    /**
     * Extract EXIF data from a photo file
     *
     * @param photoFile The photo file to extract EXIF data from
     * @return ExifData object with extracted metadata, or null if no EXIF data found
     */
    public ExifData extractExifData(File photoFile) {
        if (photoFile == null || !photoFile.exists() || !photoFile.isFile()) {
            logger.warn("Invalid photo file: {}", photoFile);
            return null;
        }

        try {
            logger.debug("Extracting EXIF data from: {}", photoFile.getName());

            Metadata metadata = ImageMetadataReader.readMetadata(photoFile);
            ExifData exifData = new ExifData();

            // Extract EXIF IFD0 data (camera info, orientation)
            ExifIFD0Directory ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0Directory != null) {
                extractCameraInfo(ifd0Directory, exifData);
                extractOrientation(ifd0Directory, exifData);
            }

            // Extract EXIF SubIFD data (date/time, exposure settings)
            ExifSubIFDDirectory subIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (subIFDDirectory != null) {
                extractDateTime(subIFDDirectory, exifData);
                extractExposureSettings(subIFDDirectory, exifData);
            }

            // Extract GPS data
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                extractGpsCoordinates(gpsDirectory, exifData);
            }

            // Check if we extracted any meaningful data
            if (hasAnyData(exifData)) {
                logger.debug("Successfully extracted EXIF data from: {}", photoFile.getName());
                return exifData;
            } else {
                logger.debug("No EXIF data found in: {}", photoFile.getName());
                return null;
            }

        } catch (ImageProcessingException e) {
            logger.warn("Error processing image metadata for {}: {}", photoFile.getName(), e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error("IO error reading metadata from {}: {}", photoFile.getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error extracting EXIF data from {}: {}", photoFile.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract camera make and model
     */
    private void extractCameraInfo(ExifIFD0Directory directory, ExifData exifData) {
        try {
            String make = directory.getString(ExifIFD0Directory.TAG_MAKE);
            if (make != null && !make.trim().isEmpty()) {
                exifData.setCameraMake(make.trim());
            }

            String model = directory.getString(ExifIFD0Directory.TAG_MODEL);
            if (model != null && !model.trim().isEmpty()) {
                exifData.setCameraModel(model.trim());
            }
        } catch (Exception e) {
            logger.debug("Error extracting camera info: {}", e.getMessage());
        }
    }

    /**
     * Extract image orientation
     */
    private void extractOrientation(ExifIFD0Directory directory, ExifData exifData) {
        try {
            Integer orientation = directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            if (orientation != null) {
                exifData.setOrientation(orientation);
            }
        } catch (Exception e) {
            logger.debug("Error extracting orientation: {}", e.getMessage());
        }
    }

    /**
     * Extract date/time original
     */
    private void extractDateTime(ExifSubIFDDirectory directory, ExifData exifData) {
        try {
            // Try to get date as Date object first
            Date dateOriginal = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (dateOriginal != null) {
                // Convert Date to LocalDateTime
                exifData.setDateTimeOriginal(new java.sql.Timestamp(dateOriginal.getTime()).toLocalDateTime());
                return;
            }

            // If Date parsing failed, try as string with various formats
            String dateString = directory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (dateString != null && !dateString.trim().isEmpty()) {
                for (DateTimeFormatter formatter : EXIF_DATE_FORMATTERS) {
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(dateString.trim(), formatter);
                        exifData.setDateTimeOriginal(dateTime);
                        return;
                    } catch (DateTimeParseException ignored) {
                        // Try next formatter
                    }
                }
                logger.debug("Could not parse date/time: {}", dateString);
            }
        } catch (Exception e) {
            logger.debug("Error extracting date/time: {}", e.getMessage());
        }
    }

    /**
     * Extract exposure settings (exposure time, f-number, ISO, focal length)
     */
    private void extractExposureSettings(ExifSubIFDDirectory directory, ExifData exifData) {
        try {
            // Exposure time
            String exposureTime = directory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
            if (exposureTime != null && !exposureTime.trim().isEmpty()) {
                exifData.setExposureTime(exposureTime.trim());
            }

            // F-Number
            String fNumber = directory.getString(ExifSubIFDDirectory.TAG_FNUMBER);
            if (fNumber != null && !fNumber.trim().isEmpty()) {
                exifData.setFNumber(fNumber.trim());
            }

            // ISO Speed
            Integer isoSpeed = directory.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
            if (isoSpeed != null) {
                exifData.setIsoSpeed(isoSpeed);
            }

            // Focal Length
            String focalLength = directory.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
            if (focalLength != null && !focalLength.trim().isEmpty()) {
                exifData.setFocalLength(focalLength.trim());
            }
        } catch (Exception e) {
            logger.debug("Error extracting exposure settings: {}", e.getMessage());
        }
    }

    /**
     * Extract and convert GPS coordinates to decimal format
     */
    private void extractGpsCoordinates(GpsDirectory directory, ExifData exifData) {
        try {
            // Get GPS location using the directory's built-in conversion
            com.drew.lang.GeoLocation geoLocation = directory.getGeoLocation();
            if (geoLocation != null) {
                // Convert to BigDecimal with appropriate precision
                BigDecimal latitude = BigDecimal.valueOf(geoLocation.getLatitude()).setScale(8, BigDecimal.ROUND_HALF_UP);
                BigDecimal longitude = BigDecimal.valueOf(geoLocation.getLongitude()).setScale(8, BigDecimal.ROUND_HALF_UP);

                exifData.setGpsLatitude(latitude);
                exifData.setGpsLongitude(longitude);
            }
        } catch (Exception e) {
            logger.debug("Error extracting GPS coordinates: {}", e.getMessage());
        }
    }

    /**
     * Check if EXIF data has any meaningful information
     */
    private boolean hasAnyData(ExifData exifData) {
        return exifData.getCameraMake() != null
            || exifData.getCameraModel() != null
            || exifData.getDateTimeOriginal() != null
            || exifData.getGpsLatitude() != null
            || exifData.getGpsLongitude() != null
            || exifData.getExposureTime() != null
            || exifData.getFNumber() != null
            || exifData.getIsoSpeed() != null
            || exifData.getFocalLength() != null
            || exifData.getOrientation() != null;
    }
}
