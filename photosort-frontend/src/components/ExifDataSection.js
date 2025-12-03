/*
 * Copyright 2025, David Snyderman
 *
 * EXIF Data Section - Read-only display of EXIF metadata
 */

import React from 'react';

const ExifDataSection = ({ exifData }) => {
  if (!exifData) {
    return null;
  }

  const formatGpsLink = (lat, lng) => {
    if (lat && lng) {
      return `https://www.google.com/maps?q=${lat},${lng}`;
    }
    return null;
  };

  const gpsLink = formatGpsLink(exifData.gpsLatitude, exifData.gpsLongitude);

  return (
    <div className="exif-data-section">
      <h3>EXIF Data</h3>
      <div className="exif-data-grid">
        {exifData.cameraMake && (
          <div className="exif-field">
            <label>Camera Make:</label>
            <span>{exifData.cameraMake}</span>
          </div>
        )}

        {exifData.cameraModel && (
          <div className="exif-field">
            <label>Camera Model:</label>
            <span>{exifData.cameraModel}</span>
          </div>
        )}

        {exifData.dateTimeOriginal && (
          <div className="exif-field">
            <label>Date/Time:</label>
            <span>{new Date(exifData.dateTimeOriginal).toLocaleString()}</span>
          </div>
        )}

        {gpsLink && (
          <div className="exif-field">
            <label>GPS Location:</label>
            <a href={gpsLink} target="_blank" rel="noopener noreferrer">
              {exifData.gpsLatitude}, {exifData.gpsLongitude}
            </a>
          </div>
        )}

        {exifData.exposureTime && (
          <div className="exif-field">
            <label>Exposure:</label>
            <span>{exifData.exposureTime}</span>
          </div>
        )}

        {exifData.fNumber && (
          <div className="exif-field">
            <label>Aperture:</label>
            <span>{exifData.fNumber}</span>
          </div>
        )}

        {exifData.isoSpeed && (
          <div className="exif-field">
            <label>ISO:</label>
            <span>{exifData.isoSpeed}</span>
          </div>
        )}

        {exifData.focalLength && (
          <div className="exif-field">
            <label>Focal Length:</label>
            <span>{exifData.focalLength}</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default ExifDataSection;
