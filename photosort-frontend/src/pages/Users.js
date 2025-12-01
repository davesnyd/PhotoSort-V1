/**
 * Users Page
 * Copyright 2025, David Snyderman
 *
 * Main page for user management - displays user table with search and pagination
 */

import React, { useState, useEffect } from 'react';
import SearchControls from '../components/SearchControls';
import UserTable from '../components/UserTable';
import PaginationControls from '../components/PaginationControls';
import userService from '../services/userService';
import '../styles/Users.css';

const Users = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Sorting state
  const [sortBy, setSortBy] = useState('email');
  const [sortDir, setSortDir] = useState('asc');

  // Search state
  const [searchMode, setSearchMode] = useState('none'); // 'none', 'quick', 'advanced'
  const [quickSearchTerm, setQuickSearchTerm] = useState('');
  const [advancedFilters, setAdvancedFilters] = useState([]);

  // Fetch users on component mount and when dependencies change
  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, sortBy, sortDir, searchMode, quickSearchTerm, advancedFilters]);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);

    try {
      const params = {
        page: currentPage,
        pageSize,
        sortBy,
        sortDir
      };

      // Add search parameters based on mode
      if (searchMode === 'quick' && quickSearchTerm) {
        params.search = quickSearchTerm;
      } else if (searchMode === 'advanced' && advancedFilters.length > 0) {
        params.filters = advancedFilters;
      }

      const response = await userService.getUsers(params);

      if (response.success) {
        const { content, totalPages: total, totalElements: count } = response.data;
        setUsers(content);
        setTotalPages(total);
        setTotalElements(count);
      } else {
        setError(response.error?.message || 'Failed to fetch users');
      }
    } catch (err) {
      console.error('Error fetching users:', err);
      setError(err.response?.data?.error?.message || 'Error loading users');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickSearch = (searchTerm) => {
    setCurrentPage(0); // Reset to first page
    if (searchTerm && searchTerm.trim() !== '') {
      setSearchMode('quick');
      setQuickSearchTerm(searchTerm);
      setAdvancedFilters([]);
    } else {
      setSearchMode('none');
      setQuickSearchTerm('');
    }
  };

  const handleAdvancedSearch = (filters) => {
    setCurrentPage(0); // Reset to first page
    if (filters && filters.length > 0) {
      setSearchMode('advanced');
      setAdvancedFilters(filters);
      setQuickSearchTerm('');
    } else {
      setSearchMode('none');
      setAdvancedFilters([]);
    }
  };

  const handleSortChange = (field) => {
    if (sortBy === field) {
      // Toggle direction
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      // New field, default to ascending
      setSortBy(field);
      setSortDir('asc');
    }
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  const handleUserTypeChange = async (userId, newUserType) => {
    try {
      const response = await userService.updateUserType(userId, newUserType);

      if (response.success) {
        // Update local state
        setUsers(users.map(user =>
          user.userId === userId ? { ...user, userType: newUserType } : user
        ));
      } else {
        alert(`Error: ${response.error?.message || 'Failed to update user type'}`);
      }
    } catch (err) {
      console.error('Error updating user type:', err);
      alert(err.response?.data?.error?.message || 'Error updating user type');
    }
  };

  return (
    <div className="users-page">
      <div className="users-header">
        <h1>User Management</h1>
        <p className="users-subtitle">
          Manage users, view photo counts, and update permissions
        </p>
      </div>

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
    </div>
  );
};

export default Users;
