/**
 * DataTable Component
 * Copyright 2025, David Snyderman
 *
 * Generic table component that renders any data with configurable columns
 * Supports sorting, custom cell rendering, and action buttons
 */

import React from 'react';
import '../styles/DataTable.css';

/**
 * Generic data table component
 * @param {Array} data - Array of data objects to display
 * @param {Array} columns - Column configuration array
 *   Each column: { field, header, sortable, render }
 *   - field: property name in data object
 *   - header: display text for column header
 *   - sortable: boolean, whether column is sortable
 *   - render: optional custom render function (row, value) => JSX
 * @param {Function} onSort - Callback for sorting (field) => void
 * @param {Object} currentSort - Current sort state { field, direction }
 * @param {Function} renderActions - Optional function to render action buttons per row
 * @param {string} keyField - Field to use as React key (default: 'id')
 * @param {string} noDataMessage - Message when no data (default: 'No data found')
 */
const DataTable = ({
  data,
  columns,
  onSort,
  currentSort,
  renderActions,
  keyField = 'id',
  noDataMessage = 'No data found'
}) => {
  /**
   * Get sort indicator icon for column header
   */
  const getSortIcon = (field) => {
    if (!currentSort || currentSort.field !== field) return ' ↕';
    return currentSort.direction === 'asc' ? ' ↑' : ' ↓';
  };

  /**
   * Handle column header click for sorting
   */
  const handleHeaderClick = (column) => {
    if (column.sortable && onSort) {
      onSort(column.field);
    }
  };

  /**
   * Render a table cell with custom or default rendering
   */
  const renderCell = (row, column) => {
    const value = row[column.field];

    if (column.render) {
      return column.render(row, value);
    }

    // Default rendering
    return value !== null && value !== undefined ? String(value) : 'N/A';
  };

  return (
    <div className="data-table-container">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((column, index) => (
              <th
                key={`header-${column.field}-${index}`}
                onClick={() => handleHeaderClick(column)}
                className={column.sortable ? 'sortable' : ''}
              >
                {column.header}
                {column.sortable && getSortIcon(column.field)}
              </th>
            ))}
            {renderActions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {data && data.length > 0 ? (
            data.map((row) => (
              <tr key={row[keyField]}>
                {columns.map((column, index) => (
                  <td key={`cell-${row[keyField]}-${column.field}-${index}`}>
                    {renderCell(row, column)}
                  </td>
                ))}
                {renderActions && (
                  <td>
                    <div className="action-buttons">
                      {renderActions(row)}
                    </div>
                  </td>
                )}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={columns.length + (renderActions ? 1 : 0)} className="no-data">
                {noDataMessage}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default DataTable;
