/**
 * Photos Page (Placeholder)
 * Copyright 2025, David Snyderman
 *
 * Placeholder page for user photos view
 * Will be implemented in a future step
 */

import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import '../styles/Photos.css';

const Photos = () => {
  const { userId } = useParams();
  const navigate = useNavigate();

  const handleBackToUsers = () => {
    navigate('/users');
  };

  return (
    <div className="photos-page">
      <div className="photos-header">
        <h1>Photos</h1>
        {userId && (
          <p className="photos-subtitle">
            Viewing photos for User ID: {userId}
          </p>
        )}
      </div>

      <div className="placeholder-content">
        <div className="placeholder-icon">📷</div>
        <h2>Photo Management</h2>
        <p>
          This page will display and manage user photos.
        </p>
        <p>
          Features will include:
        </p>
        <ul className="feature-list">
          <li>View all photos for a specific user</li>
          <li>Upload new photos</li>
          <li>Delete existing photos</li>
          <li>Edit photo metadata</li>
          <li>Set photo visibility (public/private)</li>
        </ul>
        <p className="coming-soon">
          This functionality will be implemented in Step 7.
        </p>

        <button className="back-button" onClick={handleBackToUsers}>
          ← Back to Users
        </button>
      </div>
    </div>
  );
};

export default Photos;
