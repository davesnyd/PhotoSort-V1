/**
 * TablePage Component
 * Copyright 2025, David Snyderman
 *
 * Generic page wrapper for table pages
 * Provides consistent layout for all table-based pages in the application
 */

import React from 'react';
import '../styles/TablePage.css';

/**
 * Table page wrapper component
 * @param {string} title - Page title
 * @param {string} subtitle - Page subtitle/description
 * @param {React.Node} children - Page content (table, controls, etc.)
 */
const TablePage = ({ title, subtitle, children }) => {
  return (
    <div className="table-page">
      <div className="table-page-header">
        <h1>{title}</h1>
        {subtitle && <p className="table-page-subtitle">{subtitle}</p>}
      </div>

      <div className="table-page-content">
        {children}
      </div>
    </div>
  );
};

export default TablePage;
