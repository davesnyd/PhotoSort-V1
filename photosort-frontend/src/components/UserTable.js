/**
 * UserTable Component
 * Copyright 2025, David Snyderman
 *
 * Displays user data in a sortable table with inline type editing
 * Refactored to use generic DataTable component
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DataTable from './DataTable';
import '../styles/UserTable.css';

const UserTable = ({ users, onSortChange, onUserTypeChange, currentSort }) => {
  const navigate = useNavigate();
  const [editingUserId, setEditingUserId] = useState(null);
  const [editingUserType, setEditingUserType] = useState('');

  /**
   * Format date for display
   */
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

  /**
   * Handle edit button click
   */
  const handleEdit = (user) => {
    setEditingUserId(user.userId);
    setEditingUserType(user.userType);
  };

  /**
   * Handle save button click
   */
  const handleSave = (userId) => {
    onUserTypeChange(userId, editingUserType);
    setEditingUserId(null);
    setEditingUserType('');
  };

  /**
   * Handle cancel button click
   */
  const handleCancel = () => {
    setEditingUserId(null);
    setEditingUserType('');
  };

  /**
   * Handle view images button click
   */
  const handleViewImages = (userId) => {
    navigate(`/photos/${userId}`);
  };

  /**
   * Define columns for the data table
   */
  const columns = [
    {
      field: 'userId',
      header: 'ID',
      sortable: true
    },
    {
      field: 'email',
      header: 'Email',
      sortable: true
    },
    {
      field: 'displayName',
      header: 'Display Name',
      sortable: true,
      render: (row, value) => value || 'N/A'
    },
    {
      field: 'userType',
      header: 'Type',
      sortable: true,
      render: (row) => {
        if (editingUserId === row.userId) {
          return (
            <select
              value={editingUserType}
              onChange={(e) => setEditingUserType(e.target.value)}
              className="user-type-select"
            >
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          );
        }
        return (
          <span className={`user-type-badge ${row.userType.toLowerCase()}`}>
            {row.userType}
          </span>
        );
      }
    },
    {
      field: 'photoCount',
      header: 'Photos',
      sortable: true
    },
    {
      field: 'firstLoginDate',
      header: 'First Login',
      sortable: true,
      render: (row, value) => formatDate(value)
    },
    {
      field: 'lastLoginDate',
      header: 'Last Login',
      sortable: true,
      render: (row, value) => formatDate(value)
    }
  ];

  /**
   * Render action buttons for each row
   */
  const renderActions = (row) => {
    if (editingUserId === row.userId) {
      return (
        <>
          <button
            className="action-btn save-btn"
            onClick={() => handleSave(row.userId)}
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
      );
    }

    return (
      <>
        <button
          className="action-btn edit-btn"
          onClick={() => handleEdit(row)}
        >
          Edit
        </button>
        <button
          className="action-btn view-btn"
          onClick={() => handleViewImages(row.userId)}
        >
          View Images
        </button>
      </>
    );
  };

  return (
    <DataTable
      data={users}
      columns={columns}
      onSort={onSortChange}
      currentSort={currentSort}
      renderActions={renderActions}
      keyField="userId"
      noDataMessage="No users found"
    />
  );
};

export default UserTable;
