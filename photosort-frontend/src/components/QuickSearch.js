/**
 * QuickSearch Component
 * Copyright 2025, David Snyderman
 *
 * Simple search input for filtering by email or display name
 */

import React, { useState } from 'react';
import '../styles/QuickSearch.css';

const QuickSearch = ({ onSearch }) => {
  const [searchTerm, setSearchTerm] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch(searchTerm.trim());
  };

  const handleClear = () => {
    setSearchTerm('');
    onSearch('');
  };

  return (
    <div className="quick-search">
      <form onSubmit={handleSubmit} className="quick-search-form">
        <input
          type="text"
          className="quick-search-input"
          placeholder="Search by email or name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <button type="submit" className="quick-search-btn">
          Search
        </button>
        {searchTerm && (
          <button
            type="button"
            className="quick-search-clear"
            onClick={handleClear}
          >
            Clear
          </button>
        )}
      </form>
    </div>
  );
};

export default QuickSearch;
