/**
 * OAuth Callback Handler
 * Copyright 2025, David Snyderman
 *
 * Handles the OAuth redirect from Google and completes authentication
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import authService from '../services/authService';

const OAuthCallback = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState(null);

  const handleCallback = useCallback(async () => {
    try {
      // Get the current user data from the backend
      // After successful OAuth, the backend should have created a session
      const userData = await authService.getCurrentUser();

      if (userData) {
        // Store the authentication data
        login(userData);

        // Redirect to home page
        navigate('/', { replace: true });
      } else {
        setError('Failed to retrieve user data');
      }
    } catch (error) {
      console.error('OAuth callback error:', error);
      setError('Authentication failed. Please try again.');

      // Redirect to login after a delay
      setTimeout(() => {
        navigate('/login', { replace: true });
      }, 3000);
    }
  }, [login, navigate]);

  useEffect(() => {
    handleCallback();
  }, [handleCallback]);

  if (error) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        backgroundColor: '#FFFDD0',
        flexDirection: 'column',
        gap: '20px'
      }}>
        <h2 style={{ color: '#800020' }}>Authentication Error</h2>
        <p style={{ color: '#000080' }}>{error}</p>
        <p style={{ color: '#000080' }}>Redirecting to login...</p>
      </div>
    );
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '100vh',
      backgroundColor: '#FFFDD0',
      flexDirection: 'column',
      gap: '20px'
    }}>
      <h2 style={{ color: '#800020' }}>Completing authentication...</h2>
      <div className="spinner"></div>
    </div>
  );
};

export default OAuthCallback;
