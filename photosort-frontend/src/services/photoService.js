/**
 * Photo Service
 * Copyright 2025, David Snyderman
 *
 * Handles photo management API calls
 */

import api from './api';

const photoService = {
  /**
   * Get paginated list of photos with permission filtering
   * @param {Object} params Query parameters
   * @param {number} params.page Page number (0-indexed)
   * @param {number} params.size Items per page
   * @param {string} params.sort Field to sort by
   * @param {string} params.direction Sort direction ('asc' or 'desc')
   * @param {string} params.search Quick search term (optional)
   * @param {number} params.userId Filter by owner user ID (admin only, optional)
   * @param {string} params.filterField1 Advanced filter field 1 (optional)
   * @param {string} params.filterValue1 Advanced filter value 1 (optional)
   * @param {string} params.filterType1 Advanced filter type 1 (MUST_CONTAIN or MUST_NOT_CONTAIN, optional)
   * @param {string} params.filterField2 Advanced filter field 2 (optional)
   * @param {string} params.filterValue2 Advanced filter value 2 (optional)
   * @param {string} params.filterType2 Advanced filter type 2 (optional)
   * @returns {Promise} Paginated photo list response
   */
  getPhotos: async (params = {}) => {
    try {
      const {
        page = 0,
        size = 10,
        sort = 'fileName',
        direction = 'asc',
        search = null,
        userId = null,
        filterField1 = null,
        filterValue1 = null,
        filterType1 = null,
        filterField2 = null,
        filterValue2 = null,
        filterType2 = null
      } = params;

      // Build query string
      const queryParams = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sort,
        direction
      });

      // Add optional search parameter
      if (search && search.trim() !== '') {
        queryParams.append('search', search.trim());
      }

      // Add optional userId filter (admin only)
      if (userId !== null && userId !== undefined) {
        queryParams.append('userId', userId.toString());
      }

      // Add advanced filter 1
      if (filterField1 && filterValue1 && filterType1) {
        queryParams.append('filterField1', filterField1);
        queryParams.append('filterValue1', filterValue1);
        queryParams.append('filterType1', filterType1);
      }

      // Add advanced filter 2
      if (filterField2 && filterValue2 && filterType2) {
        queryParams.append('filterField2', filterField2);
        queryParams.append('filterValue2', filterValue2);
        queryParams.append('filterType2', filterType2);
      }

      const response = await api.get(`/api/photos?${queryParams.toString()}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get user's column preferences for photo table
   * @param {number} userId User ID
   * @returns {Promise} Column preferences array
   */
  getUserColumns: async (userId) => {
    try {
      const response = await api.get(`/api/users/${userId}/columns`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update user's column preferences for photo table
   * @param {number} userId User ID
   * @param {Array} columns Array of column preference objects
   * @returns {Promise} Update confirmation
   */
  updateUserColumns: async (userId, columns) => {
    try {
      const response = await api.put(`/api/users/${userId}/columns`, columns);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default photoService;
