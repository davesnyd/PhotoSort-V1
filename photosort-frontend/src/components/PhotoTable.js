/**
 * PhotoTable Component
 * Copyright 2025, David Snyderman
 *
 * Displays photo data in a sortable table with thumbnails
 * Uses generic DataTable component
 */

import React, { useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import DataTable from './DataTable';
import photoService from '../services/photoService';
import '../styles/PhotoTable.css';

const PhotoTable = ({ photos, onSortChange, currentSort, paginationState }) => {
  const navigate = useNavigate();

  // Save pagination state before navigating to photo detail
  const handlePhotoClick = useCallback((photoId) => {
    if (paginationState) {
      sessionStorage.setItem('photoListState', JSON.stringify(paginationState));
    }
    navigate(`/photo/${photoId}`);
  }, [navigate, paginationState]);

  /**
   * Format date for display
   */
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  /**
   * Format file size for display
   */
  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return 'N/A';
    const kb = bytes / 1024;
    if (kb < 1024) return `${kb.toFixed(1)} KB`;
    const mb = kb / 1024;
    if (mb < 1024) return `${mb.toFixed(1)} MB`;
    const gb = mb / 1024;
    return `${gb.toFixed(1)} GB`;
  };

  /**
   * Format dimensions for display
   */
  const formatDimensions = (row) => {
    if (!row.imageWidth || !row.imageHeight) return 'N/A';
    return `${row.imageWidth} × ${row.imageHeight}`;
  };

  /**
   * Define columns for the data table
   */
  const columns = useMemo(() => [
    {
      field: 'thumbnail',
      header: 'Thumbnail',
      sortable: false,
      render: (row) => {
        // Only show thumbnail if thumbnailPath exists in database
        if (row.photoId && row.thumbnailPath) {
          return (
            <img
              src={photoService.getPhotoThumbnailUrl(row.photoId)}
              alt={row.fileName}
              className="photo-thumbnail"
              style={{ cursor: 'pointer' }}
              onClick={() => handlePhotoClick(row.photoId)}
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = '/placeholder-image.png';
              }}
            />
          );
        }
        return <div className="no-thumbnail">No Image</div>;
      }
    },
    {
      field: 'fileName',
      header: 'File Name',
      sortable: true,
      render: (row, value) => (
        <span
          style={{ cursor: 'pointer', color: '#0066cc' }}
          onClick={() => handlePhotoClick(row.photoId)}
        >
          {value}
        </span>
      )
    },
    {
      field: 'fileSize',
      header: 'Size',
      sortable: true,
      render: (row, value) => formatFileSize(value)
    },
    {
      field: 'dimensions',
      header: 'Dimensions',
      sortable: false,
      render: (row) => formatDimensions(row)
    },
    {
      field: 'fileCreatedDate',
      header: 'Created',
      sortable: true,
      render: (row, value) => formatDate(value)
    },
    {
      field: 'ownerDisplayName',
      header: 'Owner',
      sortable: true,
      render: (row, value) => value || 'N/A'
    },
    {
      field: 'isPublic',
      header: 'Public',
      sortable: true,
      render: (row, value) => (
        <span className={`visibility-badge ${value ? 'public' : 'private'}`}>
          {value ? 'Public' : 'Private'}
        </span>
      )
    }
  ], [navigate]); // Depends on navigate for onClick handlers

  return (
    <DataTable
      data={photos}
      columns={columns}
      onSort={onSortChange}
      currentSort={currentSort}
      keyField="photoId"
      noDataMessage="No photos found"
    />
  );
};

export default PhotoTable;
