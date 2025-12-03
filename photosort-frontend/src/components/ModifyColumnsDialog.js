/**
 * ModifyColumnsDialog Component
 * Copyright 2025, David Snyderman
 *
 * Dialog for customizing which columns appear in the photo table
 */

import React, { useState, useEffect } from 'react';
import metadataService from '../services/metadataService';
import userService from '../services/userService';
import '../styles/ModifyColumnsDialog.css';

const ModifyColumnsDialog = ({ open, onClose, userId }) => {
  const [allFields, setAllFields] = useState([]);
  const [selectedColumns, setSelectedColumns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch available fields and user's current preferences
  useEffect(() => {
    if (open) {
      fetchData();
    }
  }, [open, userId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch all available fields
      const fieldsResponse = await metadataService.getAllFields();
      if (fieldsResponse.success) {
        setAllFields(fieldsResponse.data);
      }

      // Fetch user's current column preferences
      const prefsResponse = await userService.getUserColumns(userId);
      if (prefsResponse.success) {
        const selected = prefsResponse.data.map(col => col.columnName);
        setSelectedColumns(selected);
      }
    } catch (err) {
      console.error('Error fetching column data:', err);
      setError('Failed to load column preferences');
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = (fieldName) => {
    if (selectedColumns.includes(fieldName)) {
      // Don't allow deselecting if it's the last column
      if (selectedColumns.length === 1) {
        alert('At least one column must be selected');
        return;
      }
      setSelectedColumns(selectedColumns.filter(col => col !== fieldName));
    } else {
      setSelectedColumns([...selectedColumns, fieldName]);
    }
  };

  const handleSave = async () => {
    try {
      // Convert selected columns to preferences format
      const preferences = selectedColumns.map((colName, index) => ({
        columnType: 'STANDARD', // This could be enhanced to distinguish types
        columnName: colName,
        displayOrder: index + 1
      }));

      await userService.updateUserColumns(userId, preferences);
      onClose(true); // Pass true to indicate save occurred
    } catch (err) {
      console.error('Error saving column preferences:', err);
      alert('Failed to save column preferences');
    }
  };

  const handleCancel = () => {
    onClose(false); // Pass false to indicate no save
  };

  if (!open) return null;

  return (
    <div className="dialog-overlay">
      <div className="dialog-container modify-columns-dialog">
        <div className="dialog-header">
          <h2>Customize Photo Table Columns</h2>
        </div>

        <div className="dialog-content">
          {loading && <p>Loading...</p>}

          {error && <div className="error-message">{error}</div>}

          {!loading && !error && (
            <div className="column-list">
              {allFields.map((fieldName) => (
                <div key={fieldName} className="column-item">
                  <label>
                    <input
                      type="checkbox"
                      checked={selectedColumns.includes(fieldName)}
                      onChange={() => handleToggle(fieldName)}
                    />
                    <span className="column-name">{fieldName.replace(/_/g, ' ')}</span>
                  </label>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="dialog-actions">
          <button
            className="dialog-btn save-btn"
            onClick={handleSave}
            disabled={loading || selectedColumns.length === 0}
          >
            Save
          </button>
          <button
            className="dialog-btn cancel-btn"
            onClick={handleCancel}
            disabled={loading}
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default ModifyColumnsDialog;
