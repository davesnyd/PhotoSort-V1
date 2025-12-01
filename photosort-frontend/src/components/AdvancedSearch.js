/**
 * AdvancedSearch Component
 * Copyright 2025, David Snyderman
 *
 * Advanced search with 3 filter rows for column-based filtering
 */

import React, { useState } from 'react';
import '../styles/AdvancedSearch.css';

const AdvancedSearch = ({ onSearch }) => {
  const [filters, setFilters] = useState([
    { column: 'email', value: '', operation: 'CONTAINS' },
    { column: 'displayName', value: '', operation: 'CONTAINS' },
    { column: 'userType', value: '', operation: 'CONTAINS' }
  ]);

  const columnOptions = [
    { value: 'email', label: 'Email' },
    { value: 'displayName', label: 'Display Name' },
    { value: 'userType', label: 'User Type' },
    { value: 'firstLoginDate', label: 'First Login Date' },
    { value: 'lastLoginDate', label: 'Last Login Date' }
  ];

  const operationOptions = [
    { value: 'CONTAINS', label: 'Contains' },
    { value: 'NOT_CONTAINS', label: 'Not Contains' }
  ];

  const handleFilterChange = (index, field, value) => {
    const newFilters = [...filters];
    newFilters[index][field] = value;
    setFilters(newFilters);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    // Filter out empty filters
    const activeFilters = filters.filter(f => f.value.trim() !== '');
    onSearch(activeFilters);
  };

  const handleClear = () => {
    const clearedFilters = filters.map(f => ({ ...f, value: '' }));
    setFilters(clearedFilters);
    onSearch([]);
  };

  return (
    <div className="advanced-search">
      <form onSubmit={handleSearch} className="advanced-search-form">
        <div className="advanced-search-filters">
          {filters.map((filter, index) => (
            <div key={index} className="filter-row">
              <span className="filter-label">Filter {index + 1}:</span>

              <select
                className="filter-column"
                value={filter.column}
                onChange={(e) => handleFilterChange(index, 'column', e.target.value)}
              >
                {columnOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>

              <select
                className="filter-operation"
                value={filter.operation}
                onChange={(e) => handleFilterChange(index, 'operation', e.target.value)}
              >
                {operationOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>

              <input
                type="text"
                className="filter-value"
                placeholder="Enter search value..."
                value={filter.value}
                onChange={(e) => handleFilterChange(index, 'value', e.target.value)}
              />
            </div>
          ))}
        </div>

        <div className="advanced-search-actions">
          <button type="submit" className="advanced-search-btn">
            Apply Filters
          </button>
          <button
            type="button"
            className="advanced-search-clear"
            onClick={handleClear}
          >
            Clear All
          </button>
        </div>
      </form>
    </div>
  );
};

export default AdvancedSearch;
