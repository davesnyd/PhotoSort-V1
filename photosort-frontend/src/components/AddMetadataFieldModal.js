/*
 * Copyright 2025, David Snyderman
 *
 * Add Metadata Field Modal - Dialog for adding new metadata fields
 */

import React, { useState } from 'react';

const AddMetadataFieldModal = ({ onSave, onCancel }) => {
  const [fieldName, setFieldName] = useState('');
  const [fieldValue, setFieldValue] = useState('');
  const [error, setError] = useState('');

  const handleSave = () => {
    if (!fieldName.trim()) {
      setError('Field name is required');
      return;
    }

    if (!fieldValue.trim()) {
      setError('Field value is required');
      return;
    }

    onSave(fieldName.trim(), fieldValue.trim());
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSave();
    }
  };

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h3>Add Metadata Field</h3>

        {error && <div className="modal-error">{error}</div>}

        <div className="modal-field">
          <label>Field Name:</label>
          <input
            type="text"
            value={fieldName}
            onChange={(e) => {
              setFieldName(e.target.value);
              setError('');
            }}
            onKeyPress={handleKeyPress}
            placeholder="e.g., location, event"
            autoFocus
          />
        </div>

        <div className="modal-field">
          <label>Field Value:</label>
          <input
            type="text"
            value={fieldValue}
            onChange={(e) => {
              setFieldValue(e.target.value);
              setError('');
            }}
            onKeyPress={handleKeyPress}
            placeholder="e.g., San Francisco, Wedding 2024"
          />
        </div>

        <div className="modal-buttons">
          <button onClick={onCancel} className="cancel-button">
            Cancel
          </button>
          <button onClick={handleSave} className="save-button">
            Save
          </button>
        </div>
      </div>
    </div>
  );
};

export default AddMetadataFieldModal;
