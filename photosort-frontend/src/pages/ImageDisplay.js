/*
 * Copyright 2025, David Snyderman
 *
 * Image Display Page - Step 10
 * Displays full photo with EXIF data, custom metadata, and tags
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import photoService from '../services/photoService';
import ExifDataSection from '../components/ExifDataSection';
import CustomMetadataSection from '../components/CustomMetadataSection';
import TagsSection from '../components/TagsSection';
import '../styles/ImageDisplay.css';

const ImageDisplay = () => {
  const { photoId } = useParams();
  const navigate = useNavigate();

  const [photo, setPhoto] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadPhotoDetails();
  }, [photoId]);

  const loadPhotoDetails = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await photoService.getPhotoDetail(photoId);
      if (response.success) {
        setPhoto(response.data);
      } else {
        setError('Failed to load photo details');
      }
    } catch (err) {
      setError('Error loading photo: ' + (err.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  const handleMetadataUpdate = async (metadata) => {
    try {
      const response = await photoService.updatePhotoMetadata(photoId, metadata);
      if (response.success) {
        await loadPhotoDetails(); // Reload to show updated data
      } else {
        throw new Error(response.error || 'Failed to update metadata');
      }
    } catch (err) {
      setError('Error updating metadata: ' + (err.message || 'Unknown error'));
    }
  };

  const handleTagsUpdate = async (tags) => {
    try {
      const response = await photoService.updatePhotoTags(photoId, tags);
      if (response.success) {
        await loadPhotoDetails(); // Reload to show updated data
      } else {
        throw new Error(response.error || 'Failed to update tags');
      }
    } catch (err) {
      setError('Error updating tags: ' + (err.message || 'Unknown error'));
    }
  };

  const handleReturnToList = () => {
    navigate('/photos');
  };

  if (loading) {
    return <div className="image-display-loading">Loading photo...</div>;
  }

  if (error) {
    return <div className="image-display-error">{error}</div>;
  }

  if (!photo) {
    return <div className="image-display-error">Photo not found</div>;
  }

  return (
    <div className="image-display-container">
      <div className="image-display-content">
        <div className="image-display-left">
          <img
            src={photoService.getPhotoImageUrl(photoId)}
            alt={photo.fileName}
            className="full-image"
            onError={(e) => {
              e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgZmlsbD0iI2VlZSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIxOHB4IiBmaWxsPSIjOTk5Ij5JbWFnZSBub3QgYXZhaWxhYmxlPC90ZXh0Pjwvc3ZnPg==';
            }}
          />
          <div className="image-info">
            <h2>{photo.fileName}</h2>
            {photo.imageWidth && photo.imageHeight && (
              <p>{photo.imageWidth} × {photo.imageHeight} pixels</p>
            )}
            {photo.fileSize && (
              <p>File size: {(photo.fileSize / 1024 / 1024).toFixed(2)} MB</p>
            )}
          </div>
        </div>

        <div className="image-display-right">
          <div className="metadata-panel">
            {photo.exifData && (
              <ExifDataSection exifData={photo.exifData} />
            )}

            <CustomMetadataSection
              metadata={photo.metadata || []}
              onUpdate={handleMetadataUpdate}
            />

            <TagsSection
              tags={photo.tags || []}
              onUpdate={handleTagsUpdate}
            />
          </div>
        </div>
      </div>

      <div className="image-display-footer">
        <button onClick={handleReturnToList} className="return-button">
          Return to List
        </button>
      </div>
    </div>
  );
};

export default ImageDisplay;
