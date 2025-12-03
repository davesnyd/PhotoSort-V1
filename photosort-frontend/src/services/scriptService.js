/**
 * Script Service
 * Copyright 2025, David Snyderman
 *
 * Handles script management API calls
 */

import api from './api';

const scriptService = {
  /**
   * Get all scripts
   * @param {Object} params Query parameters
   * @param {number} params.page Page number (0-indexed)
   * @param {number} params.pageSize Items per page
   * @param {string} params.sortBy Field to sort by
   * @param {string} params.sortDir Sort direction ('asc' or 'desc')
   * @param {string} params.search Quick search term (optional)
   * @returns {Promise} Script list response
   */
  getAllScripts: async (params = {}) => {
    try {
      const {
        page = 0,
        pageSize = 10,
        sortBy = 'scriptName',
        sortDir = 'asc',
        search = null
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

      const response = await api.get(`/api/scripts?${queryParams.toString()}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Get script by ID
   * @param {number} scriptId Script ID
   * @returns {Promise} Script data
   */
  getScriptById: async (scriptId) => {
    try {
      const response = await api.get(`/api/scripts/${scriptId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Create new script
   * @param {Object} script Script data
   * @returns {Promise} Created script data
   */
  createScript: async (script) => {
    try {
      const response = await api.post('/api/scripts', script);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Update existing script
   * @param {number} scriptId Script ID
   * @param {Object} script Updated script data
   * @returns {Promise} Updated script data
   */
  updateScript: async (scriptId, script) => {
    try {
      const response = await api.put(`/api/scripts/${scriptId}`, script);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Delete script
   * @param {number} scriptId Script ID
   * @returns {Promise} Success response
   */
  deleteScript: async (scriptId) => {
    try {
      const response = await api.delete(`/api/scripts/${scriptId}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default scriptService;
