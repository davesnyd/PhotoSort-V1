/**
 * Home Page Component
 * Copyright 2025, David Snyderman
 *
 * Main landing page after authentication
 */

import React from 'react';
import { useAuth } from '../context/AuthContext';
import '../styles/Home.css';

const Home = () => {
  const { user } = useAuth();

  return (
    <div className="home-container">
      <div className="home-content">
        <h1 className="home-title">Welcome to PhotoSort</h1>

        {user && (
          <div className="welcome-message">
            <h2>Hello, {user.displayName || user.email}!</h2>
            <p>Your photo management system is ready.</p>
          </div>
        )}

        <div className="feature-grid">
          <div className="feature-card">
            <h3>Photo Management</h3>
            <p>Organize and manage your photos with ease</p>
          </div>

          <div className="feature-card">
            <h3>Smart Tagging</h3>
            <p>AI-powered automatic tagging for your photos</p>
          </div>

          <div className="feature-card">
            <h3>EXIF Data</h3>
            <p>View and manage photo metadata</p>
          </div>

          <div className="feature-card">
            <h3>Search & Filter</h3>
            <p>Powerful search capabilities to find your photos</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
