/**
 * ScriptTable Component
 * Copyright 2025, David Snyderman
 *
 * Displays script data in a sortable table
 * Uses generic DataTable component
 */

import React from 'react';
import DataTable from './DataTable';
import '../styles/ScriptTable.css';

const ScriptTable = ({ scripts, onSortChange, currentSort, onEdit }) => {
  /**
   * Format run time for display (HH:MM format)
   */
  const formatRunTime = (runTime) => {
    if (!runTime) return 'N/A';
    // runTime comes as HH:MM:SS from backend
    const parts = runTime.split(':');
    return `${parts[0]}:${parts[1]}`;
  };

  /**
   * Format periodicity for display
   */
  const formatPeriodicity = (minutes) => {
    if (!minutes) return 'N/A';

    if (minutes < 60) {
      return `${minutes} min`;
    } else if (minutes === 60) {
      return '1 hour';
    } else if (minutes < 1440) {
      const hours = Math.floor(minutes / 60);
      return `${hours} hours`;
    } else {
      const days = Math.floor(minutes / 1440);
      return `${days} day${days > 1 ? 's' : ''}`;
    }
  };

  /**
   * Determine schedule type
   */
  const getScheduleType = (row) => {
    if (row.runTime) {
      return (
        <span className="schedule-badge daily">
          Daily at {formatRunTime(row.runTime)}
        </span>
      );
    } else if (row.periodicityMinutes) {
      return (
        <span className="schedule-badge periodic">
          Every {formatPeriodicity(row.periodicityMinutes)}
        </span>
      );
    } else {
      return <span className="schedule-badge manual">Manual</span>;
    }
  };

  /**
   * Define columns for the data table
   */
  const columns = [
    {
      field: 'scriptName',
      header: 'Script Name',
      sortable: true
    },
    {
      field: 'scriptFileName',
      header: 'Script File',
      sortable: true,
      render: (row, value) => value || 'N/A'
    },
    {
      field: 'schedule',
      header: 'Schedule',
      sortable: false,
      render: (row) => getScheduleType(row)
    },
    {
      field: 'fileExtension',
      header: 'File Extension',
      sortable: true,
      render: (row, value) => {
        if (!value) return 'N/A';
        return <code className="file-extension">{value}</code>;
      }
    }
  ];

  /**
   * Render action buttons for each row
   */
  const renderActions = (row) => {
    return (
      <>
        <button
          className="action-btn edit-btn"
          onClick={() => onEdit && onEdit(row)}
          title="Edit script (Step 12)"
        >
          Edit
        </button>
      </>
    );
  };

  return (
    <DataTable
      data={scripts}
      columns={columns}
      onSort={onSortChange}
      currentSort={currentSort}
      renderActions={renderActions}
      keyField="scriptId"
      noDataMessage="No scripts found"
    />
  );
};

export default ScriptTable;
