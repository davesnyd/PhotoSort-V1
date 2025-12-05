/**
 * Photo Service
 * Copyright 2025, David Snyderman
 *
 * Handles photo management API calls
 */

import api from './api';

// Get the API base URL
const getApiBaseUrl = () => {
  return process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
};

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
  },

  /**
   * Get list of user IDs who have access to a photo
   * @param {number} photoId Photo ID
   * @returns {Promise} List of user IDs with access
   */
  getPhotoPermissions: async (photoId) => {
    try {
      const response = await api.get(`/api/photos/${photoId}/permissions`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update photo permissions
   * @param {number} photoId Photo ID
   * @param {Array<number>} userIds List of user IDs to grant access
   * @returns {Promise} Success response
   */
  updatePhotoPermissions: async (photoId, userIds) => {
    try {
      const response = await api.put(`/api/photos/${photoId}/permissions`, userIds);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get complete photo details including EXIF, metadata, and tags
   * @param {number} photoId Photo ID
   * @returns {Promise} Photo detail object
   */
  getPhotoDetail: async (photoId) => {
    try {
      const response = await api.get(`/api/photos/${photoId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get photo image URL
   * @param {number} photoId Photo ID
   * @returns {string} Image URL
   */
  getPhotoImageUrl: (photoId) => {
    return `${getApiBaseUrl()}/api/photos/${photoId}/image`;
  },

  /**
   * Get photo thumbnail URL
   * @param {number} photoId Photo ID
   * @returns {string} Thumbnail URL
   */
  getPhotoThumbnailUrl: (photoId) => {
    return `${getApiBaseUrl()}/api/photos/${photoId}/thumbnail`;
  },

  /**
   * Update photo custom metadata
   * @param {number} photoId Photo ID
   * @param {Array} metadata Array of {fieldName, metadataValue} objects
   * @returns {Promise} Success response
   */
  updatePhotoMetadata: async (photoId, metadata) => {
    try {
      const response = await api.put(`/api/photos/${photoId}/metadata`, metadata);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update photo tags
   * @param {number} photoId Photo ID
   * @param {Array<string>} tags Array of tag values
   * @returns {Promise} Success response
   */
  updatePhotoTags: async (photoId, tags) => {
    try {
      const response = await api.put(`/api/photos/${photoId}/tags`, tags);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Reprocess a photo (regenerate thumbnail, re-extract EXIF, metadata, tags)
   * @param {number} photoId Photo ID
   * @returns {Promise} Success response
   */
  reprocessPhoto: async (photoId) => {
    try {
      const response = await api.post(`/api/photos/${photoId}/reprocess`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default photoService;
