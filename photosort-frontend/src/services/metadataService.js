/**
 * Metadata Service
 * Copyright 2025, David Snyderman
 *
 * Handles metadata-related API calls
 */

import api from './api';

const metadataService = {
  /**
   * Get all available metadata field names for column customization
   * @returns {Promise} List of field names
   */
  getAllFields: async () => {
    try {
      const response = await api.get('/api/metadata/fields');
      return response.data;
    } catch (error) {
      throw error;
    }
  }
};

export default metadataService;
