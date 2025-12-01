/**
 * User Service
 * Copyright 2025, David Snyderman
 *
 * Handles user management API calls
 */

import api from './api';

const userService = {
  /**
   * Get paginated list of users with optional search
   * @param {Object} params Query parameters
   * @param {number} params.page Page number (0-indexed)
   * @param {number} params.pageSize Items per page
   * @param {string} params.sortBy Field to sort by
   * @param {string} params.sortDir Sort direction ('asc' or 'desc')
   * @param {string} params.search Quick search term (optional)
   * @param {Array} params.filters Advanced search filters (optional)
   * @returns {Promise} Paginated user list response
   */
  getUsers: async (params = {}) => {
    try {
      const {
        page = 0,
        pageSize = 10,
        sortBy = 'email',
        sortDir = 'asc',
        search = null,
        filters = null
      } = params;

      // Build query string
      const queryParams = new URLSearchParams({
        page: page.toString(),
        pageSize: pageSize.toString(),
        sortBy,
        sortDir
      });

      // Add optional search parameter
      if (search && search.trim() !== '') {
        queryParams.append('search', search.trim());
      }

      // Add advanced filters if provided
      // Note: Spring Boot can accept List<SearchFilterDTO> as query params
      // but it's complex. Instead, we'll send as JSON in request body for POST
      // or encode as JSON string for GET
      if (filters && filters.length > 0) {
        queryParams.append('filters', JSON.stringify(filters));
      }

      const response = await api.get(`/api/users?${queryParams.toString()}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update a user's type (USER or ADMIN)
   * @param {number} userId User ID to update
   * @param {string} userType New user type ('USER' or 'ADMIN')
   * @returns {Promise} Updated user data
   */
  updateUserType: async (userId, userType) => {
    try {
      const response = await api.put(`/api/users/${userId}`, {
        userType: userType.toUpperCase()
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default userService;
