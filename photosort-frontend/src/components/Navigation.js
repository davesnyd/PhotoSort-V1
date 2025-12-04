/**
 * Navigation Component
 * Copyright 2025, David Snyderman
 *
 * Main navigation bar for authenticated users
 */

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Navigation.css';

const Navigation = () => {
  // TEMPORARY: Always show navigation for testing
  const SKIP_AUTH = true;

  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  // Show navigation when authenticated OR when auth is disabled for testing
  if (!isAuthenticated && !SKIP_AUTH) {
    return null;
  }

  // When auth is disabled, treat everyone as admin for testing
  const isAdmin = SKIP_AUTH || (user && user.userType === 'ADMIN');

  return (
    <nav className="navigation">
      <div className="nav-container">
        <Link to="/home" className="nav-brand">
          PhotoSort
        </Link>

        <div className="nav-links">
          <Link to="/home" className="nav-link">
            Home
          </Link>

          {/* Admin-only links */}
          {isAdmin && (
            <>
              <Link to="/users" className="nav-link">
                Users
              </Link>
              <Link to="/photos" className="nav-link">
                Photos
              </Link>
              <Link to="/scripts" className="nav-link">
                Scripts
              </Link>
              <Link to="/configuration" className="nav-link">
                Configuration
              </Link>
            </>
          )}

          {!isAdmin && (
            <Link to="/my-photos" className="nav-link">
              My Photos
            </Link>
          )}
        </div>

        <div className="nav-user">
          {user && (
            <>
              <span className="nav-username">
                {user.displayName || user.email}
              </span>
              <button className="nav-logout-btn" onClick={handleLogout}>
                Logout
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
