/**
 * useTableData Hook
 * Copyright 2025, David Snyderman
 *
 * Custom React hook for managing table state (pagination, sorting, search, loading)
 * Provides a consistent interface for all table pages in the application
 */

import { useState, useEffect, useCallback } from 'react';

/**
 * Custom hook for table data management
 * @param {Function} fetchFunction - Async function to fetch data, receives params object
 * @param {Object} initialSort - Initial sort configuration { field, direction }
 * @param {number} initialPageSize - Initial page size (default: 10)
 * @returns {Object} Table state and handlers
 */
const useTableData = (fetchFunction, initialSort = { field: 'id', direction: 'asc' }, initialPageSize = 10) => {
  // Check for saved pagination state from sessionStorage
  const getSavedState = () => {
    try {
      const saved = sessionStorage.getItem('photoListState');
      if (saved) {
        sessionStorage.removeItem('photoListState'); // Clear after reading
        return JSON.parse(saved);
      }
    } catch (e) {
      console.error('Error reading saved state:', e);
    }
    return null;
  };

  const savedState = getSavedState();

  // Data state
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Pagination state - restore from savedState if available
  const [currentPage, setCurrentPage] = useState(savedState?.currentPage ?? 0);
  const [pageSize] = useState(initialPageSize);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Sorting state - restore from savedState if available
  const [sortBy, setSortBy] = useState(savedState?.sortBy ?? initialSort.field);
  const [sortDir, setSortDir] = useState(savedState?.sortDir ?? initialSort.direction);

  // Search state
  const [searchMode, setSearchMode] = useState('none'); // 'none', 'quick', 'advanced'
  const [quickSearchTerm, setQuickSearchTerm] = useState('');
  const [advancedFilters, setAdvancedFilters] = useState([]);

  /**
   * Fetch data from the provided fetch function
   */
  const fetchData = useCallback(async (additionalParams = {}) => {
    setLoading(true);
    setError(null);

    try {
      const params = {
        page: currentPage,
        pageSize,
        sortBy,
        sortDir,
        ...additionalParams
      };

      // Add search parameters based on mode
      if (searchMode === 'quick' && quickSearchTerm) {
        params.search = quickSearchTerm;
      } else if (searchMode === 'advanced' && advancedFilters.length > 0) {
        params.filters = advancedFilters;
      }

      const response = await fetchFunction(params);

      if (response.success) {
        const { content, totalPages: total, totalElements: count } = response.data;
        setData(content);
        setTotalPages(total);
        setTotalElements(count);
      } else {
        setError(response.error?.message || 'Failed to fetch data');
      }
    } catch (err) {
      console.error('Error fetching data:', err);
      const errorMessage = err.response?.data?.error?.message || 'Error loading data';
      setError(errorMessage);
      setData([]); // Clear data on error to prevent stale data display
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, sortBy, sortDir, searchMode, quickSearchTerm, advancedFilters, fetchFunction]);

  /**
   * Fetch data when dependencies change
   */
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  /**
   * Handle quick search
   */
  const handleQuickSearch = useCallback((searchTerm) => {
    setCurrentPage(0); // Reset to first page
    if (searchTerm && searchTerm.trim() !== '') {
      setSearchMode('quick');
      setQuickSearchTerm(searchTerm);
      setAdvancedFilters([]);
    } else {
      setSearchMode('none');
      setQuickSearchTerm('');
    }
  }, []);

  /**
   * Handle advanced search
   */
  const handleAdvancedSearch = useCallback((filters) => {
    setCurrentPage(0); // Reset to first page
    if (filters && filters.length > 0) {
      setSearchMode('advanced');
      setAdvancedFilters(filters);
      setQuickSearchTerm('');
    } else {
      setSearchMode('none');
      setAdvancedFilters([]);
    }
  }, []);

  /**
   * Handle sort change
   */
  const handleSortChange = useCallback((field) => {
    if (sortBy === field) {
      // Toggle direction
      setSortDir(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      // New field, default to ascending
      setSortBy(field);
      setSortDir('asc');
    }
  }, [sortBy]);

  /**
   * Handle page change
   */
  const handlePageChange = useCallback((newPage) => {
    setCurrentPage(newPage);
  }, []);

  /**
   * Refresh data (useful after updates)
   */
  const refresh = useCallback((additionalParams = {}) => {
    fetchData(additionalParams);
  }, [fetchData]);

  return {
    // Data state
    data,
    loading,
    error,

    // Pagination state
    currentPage,
    pageSize,
    totalPages,
    totalElements,

    // Sorting state
    sortBy,
    sortDir,

    // Search state
    searchMode,
    quickSearchTerm,
    advancedFilters,

    // Handlers
    handleQuickSearch,
    handleAdvancedSearch,
    handleSortChange,
    handlePageChange,
    refresh
  };
};

export default useTableData;
