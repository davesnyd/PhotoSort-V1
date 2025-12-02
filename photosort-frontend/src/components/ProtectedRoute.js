/**
 * Protected Route Component
 * Copyright 2025, David Snyderman
 *
 * Wrapper component that protects routes requiring authentication
 */

import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, adminOnly = false }) => {
  // TEMPORARY: Disable authentication for testing
  // TODO: Re-enable when OAuth is properly configured
  const SKIP_AUTH = true;

  if (SKIP_AUTH) {
    // Bypass all authentication checks for testing
    return children;
  }

  /* PRODUCTION CODE - Uncomment when OAuth is set up:
  const { isAuthenticated, user, loading } = useAuth();

  // Show loading spinner while checking authentication
  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        backgroundColor: '#FFFDD0'
      }}>
        <h2 style={{ color: '#800020' }}>Loading...</h2>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Check admin-only routes
  if (adminOnly && user && user.userType !== 'ADMIN') {
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
        <h2 style={{ color: '#800020' }}>Access Denied</h2>
        <p style={{ color: '#000080' }}>You do not have permission to access this page.</p>
        <a href="/" style={{ color: '#800020', textDecoration: 'underline' }}>
          Return to Home
        </a>
      </div>
    );
  }
  */

  // Render the protected content
  return children;
};

export default ProtectedRoute;
