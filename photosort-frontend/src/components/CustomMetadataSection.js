/*
 * Copyright 2025, David Snyderman
 *
 * Custom Metadata Section - Editable metadata fields
 */

import React, { useState } from 'react';
import AddMetadataFieldModal from './AddMetadataFieldModal';

const CustomMetadataSection = ({ metadata, onUpdate }) => {
  const [editingId, setEditingId] = useState(null);
  const [editValue, setEditValue] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);

  const handleEdit = (field) => {
    setEditingId(field.metadataId);
    setEditValue(field.metadataValue);
  };

  const handleSaveEdit = (field) => {
    const updatedMetadata = metadata.map(m =>
      m.metadataId === field.metadataId
        ? { ...m, metadataValue: editValue }
        : m
    );
    onUpdate(updatedMetadata);
    setEditingId(null);
    setEditValue('');
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditValue('');
  };

  const handleDelete = (field) => {
    const updatedMetadata = metadata.filter(m => m.metadataId !== field.metadataId);
    onUpdate(updatedMetadata);
  };

  const handleAddField = (fieldName, fieldValue) => {
    const updatedMetadata = [...metadata, { fieldName, metadataValue: fieldValue }];
    onUpdate(updatedMetadata);
    setShowAddModal(false);
  };

  return (
    <div className="custom-metadata-section">
      <h3>Custom Metadata</h3>
      <div className="metadata-list">
        {metadata.length === 0 && (
          <p className="no-metadata">No custom metadata fields</p>
        )}

        {metadata.map((field) => (
          <div key={field.metadataId || field.fieldName} className="metadata-field">
            <label>{field.fieldName}:</label>
            {editingId === field.metadataId ? (
              <div className="edit-controls">
                <input
                  type="text"
                  value={editValue}
                  onChange={(e) => setEditValue(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      handleSaveEdit(field);
                    }
                  }}
                  autoFocus
                />
                <button onClick={() => handleSaveEdit(field)} className="save-btn">
                  ✓
                </button>
                <button onClick={handleCancelEdit} className="cancel-btn">
                  ✕
                </button>
              </div>
            ) : (
              <div className="display-controls">
                <span>{field.metadataValue}</span>
                <button onClick={() => handleEdit(field)} className="edit-btn" title="Edit">
                  ✎
                </button>
                <button onClick={() => handleDelete(field)} className="delete-btn" title="Delete">
                  🗑
                </button>
              </div>
            )}
          </div>
        ))}
      </div>

      <button onClick={() => setShowAddModal(true)} className="add-field-btn">
        Add Field
      </button>

      {showAddModal && (
        <AddMetadataFieldModal
          onSave={handleAddField}
          onCancel={() => setShowAddModal(false)}
        />
      )}
    </div>
  );
};

export default CustomMetadataSection;
