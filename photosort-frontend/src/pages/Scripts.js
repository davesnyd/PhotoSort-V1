/**
 * Scripts Page
 * Copyright 2025, David Snyderman
 *
 * Main page for script management - displays script table
 * Note: Currently implements client-side pagination since backend returns all scripts
 */

import React from 'react';
import TablePage from '../components/TablePage';
import SearchControls from '../components/SearchControls';
import ScriptTable from '../components/ScriptTable';
import PaginationControls from '../components/PaginationControls';
import useTableData from '../hooks/useTableData';
import scriptService from '../services/scriptService';

const Scripts = () => {

  // Wrapper function to adapt script service response to PagedResponse format
  const fetchScriptsWithPagination = async (params) => {
    try {
      const response = await scriptService.getAllScripts(params);

      if (response.success) {
        // Backend returns simple list, so we need to paginate client-side
        const allScripts = response.data;

        // Apply client-side search if provided
        let filteredScripts = allScripts;
        if (params.search) {
          const searchLower = params.search.toLowerCase();
          filteredScripts = allScripts.filter(script =>
            script.scriptName.toLowerCase().includes(searchLower) ||
            (script.scriptFileName && script.scriptFileName.toLowerCase().includes(searchLower)) ||
            (script.fileExtension && script.fileExtension.toLowerCase().includes(searchLower))
          );
        }

        // Apply client-side sorting
        const sortedScripts = [...filteredScripts].sort((a, b) => {
          const aVal = a[params.sortBy] || '';
          const bVal = b[params.sortBy] || '';

          if (params.sortDir === 'asc') {
            return aVal > bVal ? 1 : aVal < bVal ? -1 : 0;
          } else {
            return aVal < bVal ? 1 : aVal > bVal ? -1 : 0;
          }
        });

        // Apply client-side pagination
        const start = params.page * params.pageSize;
        const end = start + params.pageSize;
        const paginatedScripts = sortedScripts.slice(start, end);
        const totalPages = Math.ceil(sortedScripts.length / params.pageSize);

        return {
          success: true,
          data: {
            content: paginatedScripts,
            totalPages: totalPages,
            totalElements: sortedScripts.length
          }
        };
      }

      return response;
    } catch (error) {
      throw error;
    }
  };

  // Use the generic useTableData hook with wrapper function
  const {
    data: scripts,
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
    handlePageChange
  } = useTableData(
    fetchScriptsWithPagination,
    { field: 'scriptName', direction: 'asc' },
    10
  );

  /**
   * Handle edit button click (placeholder for Step 12)
   */
  const handleEdit = (script) => {
    alert(`Edit functionality will be implemented in Step 12.\nScript: ${script.scriptName}`);
  };

  /**
   * Handle add script button (placeholder for Step 12)
   */
  const handleAddScript = () => {
    alert('Add Script functionality will be implemented in Step 12.');
  };

  return (
    <TablePage
      title="Script Management"
      subtitle="Manage automated scripts for photo processing"
    >
      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={handleAddScript}
          style={{
            padding: '10px 20px',
            backgroundColor: '#4CAF50',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: '500'
          }}
        >
          Add Script
        </button>
      </div>

      <SearchControls
        onQuickSearch={handleQuickSearch}
        onAdvancedSearch={handleAdvancedSearch}
      />

      {loading && <div className="loading-message">Loading scripts...</div>}

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {!loading && !error && (
        <>
          <div className="results-summary">
            Showing {scripts.length} of {totalElements} script{totalElements !== 1 ? 's' : ''}
          </div>

          <ScriptTable
            scripts={scripts}
            onSortChange={handleSortChange}
            currentSort={{ field: sortBy, direction: sortDir }}
            onEdit={handleEdit}
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

export default Scripts;
