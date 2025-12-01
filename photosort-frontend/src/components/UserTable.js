/**
 * UserTable Component
 * Copyright 2025, David Snyderman
 *
 * Displays user data in a sortable table with inline type editing
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/UserTable.css';

const UserTable = ({ users, onSortChange, onUserTypeChange, currentSort }) => {
  const navigate = useNavigate();
  const [editingUserId, setEditingUserId] = useState(null);
  const [editingUserType, setEditingUserType] = useState('');

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleSort = (field) => {
    onSortChange(field);
  };

  const handleEdit = (user) => {
    setEditingUserId(user.userId);
    setEditingUserType(user.userType);
  };

  const handleSave = (userId) => {
    onUserTypeChange(userId, editingUserType);
    setEditingUserId(null);
    setEditingUserType('');
  };

  const handleCancel = () => {
    setEditingUserId(null);
    setEditingUserType('');
  };

  const handleViewImages = (userId) => {
    navigate(`/photos/${userId}`);
  };

  const getSortIcon = (field) => {
    if (currentSort.field !== field) return ' ↕';
    return currentSort.direction === 'asc' ? ' ↑' : ' ↓';
  };

  return (
    <div className="user-table-container">
      <table className="user-table">
        <thead>
          <tr>
            <th onClick={() => handleSort('userId')} className="sortable">
              ID{getSortIcon('userId')}
            </th>
            <th onClick={() => handleSort('email')} className="sortable">
              Email{getSortIcon('email')}
            </th>
            <th onClick={() => handleSort('displayName')} className="sortable">
              Display Name{getSortIcon('displayName')}
            </th>
            <th onClick={() => handleSort('userType')} className="sortable">
              Type{getSortIcon('userType')}
            </th>
            <th onClick={() => handleSort('photoCount')} className="sortable">
              Photos{getSortIcon('photoCount')}
            </th>
            <th onClick={() => handleSort('firstLoginDate')} className="sortable">
              First Login{getSortIcon('firstLoginDate')}
            </th>
            <th onClick={() => handleSort('lastLoginDate')} className="sortable">
              Last Login{getSortIcon('lastLoginDate')}
            </th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users && users.length > 0 ? (
            users.map(user => (
              <tr key={user.userId}>
                <td>{user.userId}</td>
                <td>{user.email}</td>
                <td>{user.displayName || 'N/A'}</td>
                <td>
                  {editingUserId === user.userId ? (
                    <select
                      value={editingUserType}
                      onChange={(e) => setEditingUserType(e.target.value)}
                      className="user-type-select"
                    >
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  ) : (
                    <span className={`user-type-badge ${user.userType.toLowerCase()}`}>
                      {user.userType}
                    </span>
                  )}
                </td>
                <td>{user.photoCount}</td>
                <td>{formatDate(user.firstLoginDate)}</td>
                <td>{formatDate(user.lastLoginDate)}</td>
                <td>
                  <div className="action-buttons">
                    {editingUserId === user.userId ? (
                      <>
                        <button
                          className="action-btn save-btn"
                          onClick={() => handleSave(user.userId)}
                        >
                          Save
                        </button>
                        <button
                          className="action-btn cancel-btn"
                          onClick={handleCancel}
                        >
                          Cancel
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          className="action-btn edit-btn"
                          onClick={() => handleEdit(user)}
                        >
                          Edit
                        </button>
                        <button
                          className="action-btn view-btn"
                          onClick={() => handleViewImages(user.userId)}
                        >
                          View Images
                        </button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="8" className="no-data">
                No users found
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default UserTable;
