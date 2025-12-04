/**
 * Landing Page Component
 * Copyright 2025, David Snyderman
 *
 * Initial landing page that redirects to login or home based on auth status
 */

import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Landing = () => {
  const { isAuthenticated, loading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!loading) {
      if (isAuthenticated) {
        navigate('/home', { replace: true });
      } else {
        navigate('/login', { replace: true });
      }
    }
  }, [isAuthenticated, loading, navigate]);

  // Show nothing while redirecting (prevents flicker)
  return null;
};

export default Landing;
