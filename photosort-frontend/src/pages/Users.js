/**
 * Users Page
 * Copyright 2025, David Snyderman
 *
 * Main page for user management - displays user table with search and pagination
 * Refactored to use generic useTableData hook and TablePage component
 */

import React from 'react';
import TablePage from '../components/TablePage';
import SearchControls from '../components/SearchControls';
import UserTable from '../components/UserTable';
import PaginationControls from '../components/PaginationControls';
import useTableData from '../hooks/useTableData';
import userService from '../services/userService';

const Users = () => {
  // Use the generic useTableData hook
  const {
    data: users,
    loading,
    error,
    currentPage,
    totalPages,
    totalElements,
    sortBy,
    sortDir,
    handleQuickSearch,
    handleAdvancedSearch,
    handleSortChange,
    handlePageChange,
    refresh
  } = useTableData(
    userService.getUsers,
    { field: 'email', direction: 'asc' },
    10
  );

  /**
   * Handle user type change
   */
  const handleUserTypeChange = async (userId, newUserType) => {
    try {
      const response = await userService.updateUserType(userId, newUserType);

      if (response.success) {
        // Refresh the table data
        refresh();
      } else {
        alert(`Error: ${response.error?.message || 'Failed to update user type'}`);
      }
    } catch (err) {
      console.error('Error updating user type:', err);
      alert(err.response?.data?.error?.message || 'Error updating user type');
    }
  };

  return (
    <TablePage
      title="User Management"
      subtitle="Manage users, view photo counts, and update permissions"
    >
      <SearchControls
        onQuickSearch={handleQuickSearch}
        onAdvancedSearch={handleAdvancedSearch}
      />

      {loading && <div className="loading-message">Loading users...</div>}

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {!loading && !error && (
        <>
          <div className="results-summary">
            Showing {users.length} of {totalElements} user{totalElements !== 1 ? 's' : ''}
          </div>

          <UserTable
            users={users}
            onSortChange={handleSortChange}
            onUserTypeChange={handleUserTypeChange}
            currentSort={{ field: sortBy, direction: sortDir }}
          />

          <PaginationControls
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </TablePage>
  );
};

export default Users;
