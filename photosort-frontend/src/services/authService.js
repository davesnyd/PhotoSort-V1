/**
 * Authentication Service
 * Copyright 2025, David Snyderman
 *
 * Handles authentication-related API calls
 */

import api from './api';

const authService = {
  /**
   * Initiate Google OAuth login
   * Redirects to backend OAuth endpoint
   * Uses relative path for Docker deployment (nginx proxies to backend)
   */
  loginWithGoogle: () => {
    window.location.href = '/oauth2/authorization/google';
  },

  /**
   * Get current authenticated user
   * @returns {Promise} User data
   */
  getCurrentUser: async () => {
    try {
      const response = await api.get('/api/auth/current');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Logout current user
   * @returns {Promise} Logout response
   */
  logout: async () => {
    try {
      const response = await api.post('/api/auth/logout');
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  /**
   * Check if user is authenticated
   * @returns {boolean} Authentication status
   */
  isAuthenticated: () => {
    return localStorage.getItem('authToken') !== null;
  },

  /**
   * Get stored user data
   * @returns {Object|null} User data or null
   */
  getStoredUser: () => {
    const userJson = localStorage.getItem('user');
    return userJson ? JSON.parse(userJson) : null;
  },

  /**
   * Store user data and token
   * @param {Object} user User data
   * @param {string} token Authentication token (optional)
   */
  storeAuth: (user, token) => {
    localStorage.setItem('user', JSON.stringify(user));
    if (token) {
      localStorage.setItem('authToken', token);
    }
  },
};

export default authService;
