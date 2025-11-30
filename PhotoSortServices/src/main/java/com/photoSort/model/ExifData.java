/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing EXIF metadata extracted from a photo.
 * EXIF data includes camera settings, GPS coordinates, and other technical information.
 */
@Entity
@Table(name = "exif_data", indexes = {
    @Index(name = "idx_exif_data_photo_id", columnList = "photo_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExifData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exif_id")
    private Long exifId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Photo photo;

    @Column(name = "date_time_original")
    private LocalDateTime dateTimeOriginal;

    @Column(name = "camera_make", length = 100)
    private String cameraMake;

    @Column(name = "camera_model", length = 100)
    private String cameraModel;

    @Column(name = "gps_latitude", precision = 10, scale = 8)
    private BigDecimal gpsLatitude;

    @Column(name = "gps_longitude", precision = 11, scale = 8)
    private BigDecimal gpsLongitude;

    @Column(name = "exposure_time", length = 50)
    private String exposureTime;

    @Column(name = "f_number", length = 50)
    private String fNumber;

    @Column(name = "iso_speed")
    private Integer isoSpeed;

    @Column(name = "focal_length", length = 50)
    private String focalLength;

    @Column(name = "orientation")
    private Integer orientation;
}
