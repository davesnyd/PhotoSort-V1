/**
 * PaginationControls Component
 * Copyright 2025, David Snyderman
 *
 * Reusable pagination controls for navigating through paged data
 */

import React from 'react';
import '../styles/PaginationControls.css';

const PaginationControls = ({ currentPage, totalPages, onPageChange }) => {
  const handlePrevious = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages - 1) {
      onPageChange(currentPage + 1);
    }
  };

  const handleFirst = () => {
    onPageChange(0);
  };

  const handleLast = () => {
    onPageChange(totalPages - 1);
  };

  // Generate page numbers to display
  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible) {
      // Show all pages
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Show subset with current page in middle
      let start = Math.max(0, currentPage - 2);
      let end = Math.min(totalPages - 1, start + maxVisible - 1);

      // Adjust start if we're near the end
      if (end === totalPages - 1) {
        start = Math.max(0, end - maxVisible + 1);
      }

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }

    return pages;
  };

  if (totalPages <= 1) {
    return null; // Don't show pagination if only one page
  }

  return (
    <div className="pagination-controls">
      <button
        className="pagination-btn"
        onClick={handleFirst}
        disabled={currentPage === 0}
      >
        &laquo; First
      </button>

      <button
        className="pagination-btn"
        onClick={handlePrevious}
        disabled={currentPage === 0}
      >
        &lsaquo; Prev
      </button>

      <div className="pagination-pages">
        {getPageNumbers().map(page => (
          <button
            key={page}
            className={`pagination-page ${page === currentPage ? 'active' : ''}`}
            onClick={() => onPageChange(page)}
          >
            {page + 1}
          </button>
        ))}
      </div>

      <button
        className="pagination-btn"
        onClick={handleNext}
        disabled={currentPage >= totalPages - 1}
      >
        Next &rsaquo;
      </button>

      <button
        className="pagination-btn"
        onClick={handleLast}
        disabled={currentPage >= totalPages - 1}
      >
        Last &raquo;
      </button>

      <span className="pagination-info">
        Page {currentPage + 1} of {totalPages}
      </span>
    </div>
  );
};

export default PaginationControls;
