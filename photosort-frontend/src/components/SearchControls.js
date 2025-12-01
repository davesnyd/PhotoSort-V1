/**
 * SearchControls Component
 * Copyright 2025, David Snyderman
 *
 * Combines QuickSearch and AdvancedSearch with tabbed interface
 */

import React, { useState } from 'react';
import QuickSearch from './QuickSearch';
import AdvancedSearch from './AdvancedSearch';
import '../styles/SearchControls.css';

const SearchControls = ({ onQuickSearch, onAdvancedSearch }) => {
  const [activeTab, setActiveTab] = useState('quick');

  const handleQuickSearch = (searchTerm) => {
    onQuickSearch(searchTerm);
  };

  const handleAdvancedSearch = (filters) => {
    onAdvancedSearch(filters);
  };

  return (
    <div className="search-controls">
      <div className="search-tabs">
        <button
          className={`search-tab ${activeTab === 'quick' ? 'active' : ''}`}
          onClick={() => setActiveTab('quick')}
        >
          Quick Search
        </button>
        <button
          className={`search-tab ${activeTab === 'advanced' ? 'active' : ''}`}
          onClick={() => setActiveTab('advanced')}
        >
          Advanced Search
        </button>
      </div>

      <div className="search-content">
        {activeTab === 'quick' ? (
          <QuickSearch onSearch={handleQuickSearch} />
        ) : (
          <AdvancedSearch onSearch={handleAdvancedSearch} />
        )}
      </div>
    </div>
  );
};

export default SearchControls;
