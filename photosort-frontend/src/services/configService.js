/**
 * Configuration Service
 * Copyright 2025, David Snyderman
 *
 * Service for managing system configuration (Step 13)
 */

import api from './api';

/**
 * Get current system configuration
 * Returns configuration with passwords redacted as "********"
 * @returns {Promise} Configuration data
 */
export const getConfiguration = async () => {
  const response = await api.get('/api/config');
  return response.data;
};

/**
 * Update system configuration
 * Only updates password fields if value is not "********"
 * @param {Object} configData - Configuration data to update
 * @returns {Promise} Updated configuration
 */
export const updateConfiguration = async (configData) => {
  const response = await api.put('/api/config', configData);
  return response.data;
};

/**
 * Trigger a full rescan of the photo repository
 * Clears poll state and processes all photos in the configured directory
 * @returns {Promise} Rescan result with photosProcessed count
 */
export const triggerRescan = async () => {
  const response = await api.post('/api/config/rescan');
  return response.data;
};

export default {
  getConfiguration,
  updateConfiguration,
  triggerRescan
};
