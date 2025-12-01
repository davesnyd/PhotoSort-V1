/**
 * Authentication Context
 * Copyright 2025, David Snyderman
 *
 * Provides authentication state and methods throughout the application
 */

import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import authService from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Error during logout:', error);
    } finally {
      setUser(null);
      setIsAuthenticated(false);
    }
  }, []);

  const checkAuthStatus = useCallback(async () => {
    try {
      // Check if we have stored auth data
      const storedUser = authService.getStoredUser();
      const hasToken = authService.isAuthenticated();

      if (hasToken && storedUser) {
        // Verify with backend that session is still valid
        try {
          const userData = await authService.getCurrentUser();
          setUser(userData);
          setIsAuthenticated(true);
        } catch (error) {
          // Session expired or invalid
          logout();
        }
      }
    } catch (error) {
      console.error('Error checking auth status:', error);
    } finally {
      setLoading(false);
    }
  }, [logout]);

  // Check authentication status on component mount
  useEffect(() => {
    checkAuthStatus();
  }, [checkAuthStatus]);

  const login = (userData, token) => {
    authService.storeAuth(userData, token);
    setUser(userData);
    setIsAuthenticated(true);
  };

  const loginWithGoogle = () => {
    authService.loginWithGoogle();
  };

  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    logout,
    loginWithGoogle,
    checkAuthStatus,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Custom hook to use the auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
