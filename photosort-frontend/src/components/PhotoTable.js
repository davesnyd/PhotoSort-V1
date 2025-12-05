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

const PhotoTable = ({ photos, onSortChange, currentSort }) => {
  const navigate = useNavigate();

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
        if (row.photoId) {
          return (
            <img
              src={photoService.getPhotoThumbnailUrl(row.photoId)}
              alt={row.fileName}
              className="photo-thumbnail"
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
      sortable: true
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
  ], []); // Empty dependency array - columns config is static

  /**
   * Render action buttons for each row
   */
  const renderActions = useCallback((row) => {
    return (
      <>
        <button
          className="action-btn view-btn"
          onClick={() => navigate(`/photo/${row.photoId}`)}
          title="View full size image with metadata"
        >
          View
        </button>
      </>
    );
  }, [navigate]); // Depends on navigate function

  return (
    <DataTable
      data={photos}
      columns={columns}
      onSort={onSortChange}
      currentSort={currentSort}
      renderActions={renderActions}
      keyField="photoId"
      noDataMessage="No photos found"
    />
  );
};

export default PhotoTable;
