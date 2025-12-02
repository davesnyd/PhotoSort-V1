/**
 * API Service Configuration
 * Copyright 2025, David Snyderman
 *
 * Configures Axios instance with base URL and authentication interceptors
 */

import axios from 'axios';

// Create Axios instance with base configuration
// TEMPORARY: Using port 8081 and disabling auth interceptors for testing
const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Important for session-based auth
});

// TEMPORARY: Authentication interceptors disabled for testing
// TODO: Re-enable when OAuth is properly configured

/* PRODUCTION CODE - Uncomment when OAuth is set up:
// Request interceptor to add authentication token if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle authentication errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Unauthorized - redirect to login
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
*/

export default api;
