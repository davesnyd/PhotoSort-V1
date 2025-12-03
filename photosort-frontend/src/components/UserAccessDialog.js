/**
 * User Access Dialog Component
 * Copyright 2025, David Snyderman
 *
 * Dialog for managing which users can access a private photo
 */

import React, { useState, useEffect } from 'react';
import photoService from '../services/photoService';
import userService from '../services/userService';
import '../styles/UserAccessDialog.css';

const UserAccessDialog = ({ photoId, photoFilename, onClose, onSave }) => {
  const [users, setUsers] = useState([]);
  const [selectedUserIds, setSelectedUserIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadData();
  }, [photoId]);

  /**
   * Load users and current permissions
   */
  const loadData = async () => {
    setLoading(true);
    setError(null);

    try {
      // Get all users
      const usersResponse = await userService.getAllUsers();
      if (usersResponse.success) {
        setUsers(usersResponse.data);
      } else {
        setError('Failed to load users');
        return;
      }

      // Get current permissions for this photo
      const permissionsResponse = await photoService.getPhotoPermissions(photoId);
      if (permissionsResponse.success) {
        setSelectedUserIds(permissionsResponse.data);
      } else {
        setError('Failed to load permissions');
      }
    } catch (err) {
      setError('Error loading data: ' + (err.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  /**
   * Toggle user permission
   */
  const handleToggleUser = (userId) => {
    setSelectedUserIds(prevSelected => {
      if (prevSelected.includes(userId)) {
        return prevSelected.filter(id => id !== userId);
      } else {
        return [...prevSelected, userId];
      }
    });
  };

  /**
   * Save permissions
   */
  const handleSave = async () => {
    setSaving(true);
    setError(null);

    try {
      const response = await photoService.updatePhotoPermissions(photoId, selectedUserIds);
      if (response.success) {
        if (onSave) {
          onSave();
        }
        onClose();
      } else {
        setError('Failed to save permissions: ' + (response.error || 'Unknown error'));
      }
    } catch (err) {
      setError('Error saving permissions: ' + (err.message || 'Unknown error'));
    } finally {
      setSaving(false);
    }
  };

  /**
   * Cancel without saving
   */
  const handleCancel = () => {
    onClose();
  };

  return (
    <div className="user-access-dialog-overlay" data-testid="user-access-dialog-overlay">
      <div className="user-access-dialog" data-testid="user-access-dialog">
        <div className="user-access-dialog-header">
          <h2>Manage Photo Access - {photoFilename}</h2>
        </div>

        <div className="user-access-dialog-content">
          {loading && (
            <div className="user-access-dialog-loading" data-testid="loading-indicator">
              Loading...
            </div>
          )}

          {error && (
            <div className="user-access-dialog-error" data-testid="error-message">
              {error}
            </div>
          )}

          {!loading && !error && users.length === 0 && (
            <div className="user-access-dialog-empty" data-testid="no-users-message">
              No users available
            </div>
          )}

          {!loading && !error && users.length > 0 && (
            <div className="user-list" data-testid="user-list">
              {users.map(user => (
                <div key={user.userId} className="user-row" data-testid={`user-row-${user.userId}`}>
                  <input
                    type="checkbox"
                    id={`user-${user.userId}`}
                    checked={selectedUserIds.includes(user.userId)}
                    onChange={() => handleToggleUser(user.userId)}
                    data-testid={`checkbox-${user.userId}`}
                  />
                  <label htmlFor={`user-${user.userId}`} className="user-info">
                    <span className="user-name">{user.displayName}</span>
                    <span className="user-email">{user.email}</span>
                  </label>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="user-access-dialog-footer">
          <button
            onClick={handleCancel}
            disabled={saving}
            className="cancel-button"
            data-testid="cancel-button"
          >
            Cancel
          </button>
          <button
            onClick={handleSave}
            disabled={saving || loading}
            className="save-button"
            data-testid="save-button"
          >
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserAccessDialog;
