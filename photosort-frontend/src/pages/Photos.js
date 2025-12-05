/**
 * Photos Page
 * Copyright 2025, David Snyderman
 *
 * Main page for photo management - displays photo table with search, pagination,
 * and permission-based filtering
 * Uses generic useTableData hook and TablePage component
 */

import React, { useCallback, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import TablePage from '../components/TablePage';
import SearchControls from '../components/SearchControls';
import PhotoTable from '../components/PhotoTable';
import PaginationControls from '../components/PaginationControls';
import useTableData from '../hooks/useTableData';
import photoService from '../services/photoService';

const Photos = () => {
  const { userId } = useParams();

  // Use the generic useTableData hook with photo service
  // If userId is provided, filter photos by that user (admin feature)
  const fetchFunction = useCallback((params) => {
    if (userId) {
      return photoService.getPhotos({ ...params, userId: parseInt(userId) });
    }
    return photoService.getPhotos(params);
  }, [userId]); // Dependency on userId since it's used inside the function

  const {
    data: photos,
    loading,
    error,
    currentPage,
    totalPages,
    totalElements,
    sortBy,
    sortDir,
    handleQuickSearch,
    handleAdvancedSearch,
    handleSortChange,
    handlePageChange
  } = useTableData(
    fetchFunction,
    { field: 'fileName', direction: 'asc' },
    10
  );

  // Determine page title and subtitle
  const title = userId ? `Photos for User ${userId}` : 'My Photos';
  const subtitle = userId
    ? 'Viewing all photos for this user'
    : 'View and manage your photos with permission-based access';

  // Memoize currentSort object to prevent unnecessary re-renders
  const currentSort = useMemo(() => ({ field: sortBy, direction: sortDir }), [sortBy, sortDir]);

  // Memoize pagination state to pass to PhotoTable
  const paginationState = useMemo(() => ({
    currentPage,
    sortBy,
    sortDir
  }), [currentPage, sortBy, sortDir]);

  return (
    <TablePage title={title} subtitle={subtitle}>
      <SearchControls
        onQuickSearch={handleQuickSearch}
        onAdvancedSearch={handleAdvancedSearch}
      />

      {loading && <div className="loading-message">Loading photos...</div>}

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {!loading && !error && (
        <>
          <div className="results-summary">
            Showing {photos.length} of {totalElements} photo{totalElements !== 1 ? 's' : ''}
          </div>

          <PhotoTable
            photos={photos}
            onSortChange={handleSortChange}
            currentSort={currentSort}
            paginationState={paginationState}
          />

          <PaginationControls
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </TablePage>
  );
};

export default Photos;
