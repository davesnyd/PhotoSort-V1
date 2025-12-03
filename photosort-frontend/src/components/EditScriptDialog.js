/**
 * Edit Script Dialog Component
 * Copyright 2025, David Snyderman
 *
 * Dialog for creating/editing automated scripts
 */

import React, { useState, useEffect } from 'react';
import scriptService from '../services/scriptService';
import '../styles/EditScriptDialog.css';

const EditScriptDialog = ({ script, onClose, onSave }) => {
  const [scriptName, setScriptName] = useState('');
  const [scriptFileName, setScriptFileName] = useState('');
  const [runTime, setRunTime] = useState('');
  const [periodicityMinutes, setPeriodicityMinutes] = useState('');
  const [fileExtension, setFileExtension] = useState('');
  const [scriptContents, setScriptContents] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [validationError, setValidationError] = useState('');

  const isEditMode = script !== null;

  /**
   * Load script data when editing
   */
  useEffect(() => {
    if (script) {
      setScriptName(script.scriptName || '');
      setScriptFileName(script.scriptFileName || '');
      setFileExtension(script.fileExtension || '');
      setScriptContents(script.scriptContents || '');

      // Convert runTime from HH:MM:SS to HH:MM for time input
      if (script.runTime) {
        const timeParts = script.runTime.split(':');
        setRunTime(`${timeParts[0]}:${timeParts[1]}`);
        setPeriodicityMinutes('');
      } else if (script.periodicityMinutes) {
        setPeriodicityMinutes(script.periodicityMinutes.toString());
        setRunTime('');
      }
    }
  }, [script]);

  /**
   * Handle run time change (clears periodicity)
   */
  const handleRunTimeChange = (e) => {
    const value = e.target.value;
    setRunTime(value);
    if (value) {
      setPeriodicityMinutes('');
    }
    setValidationError('');
  };

  /**
   * Handle periodicity change (clears run time)
   */
  const handlePeriodicityChange = (e) => {
    const value = e.target.value;
    setPeriodicityMinutes(value);
    if (value) {
      setRunTime('');
    }
    setValidationError('');
  };

  /**
   * Validate form
   */
  const validateForm = () => {
    if (!scriptName.trim()) {
      setValidationError('Script name is required');
      return false;
    }
    return true;
  };

  /**
   * Build script object for API
   */
  const buildScriptObject = () => {
    const scriptData = {
      scriptName: scriptName.trim(),
      scriptFileName: scriptFileName.trim() || null,
      fileExtension: fileExtension.trim() || null,
      scriptContents: scriptContents || null,
      runTime: null,
      periodicityMinutes: null
    };

    // Convert runTime from HH:MM to HH:MM:SS
    if (runTime) {
      scriptData.runTime = `${runTime}:00`;
    } else if (periodicityMinutes) {
      scriptData.periodicityMinutes = parseInt(periodicityMinutes, 10);
    }

    return scriptData;
  };

  /**
   * Handle save
   */
  const handleSave = async () => {
    if (!validateForm()) {
      return;
    }

    setSaving(true);
    setError(null);

    try {
      const scriptData = buildScriptObject();
      let response;

      if (isEditMode) {
        response = await scriptService.updateScript(script.scriptId, scriptData);
      } else {
        response = await scriptService.createScript(scriptData);
      }

      if (response.success) {
        if (onSave) {
          onSave();
        }
        onClose();
      } else {
        setError(response.error || 'Failed to save script');
      }
    } catch (err) {
      setError('Error saving script: ' + (err.message || 'Unknown error'));
    } finally {
      setSaving(false);
    }
  };

  /**
   * Handle delete
   */
  const handleDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete script '${scriptName}'?`)) {
      return;
    }

    setSaving(true);
    setError(null);

    try {
      const response = await scriptService.deleteScript(script.scriptId);

      if (response.success) {
        if (onSave) {
          onSave();
        }
        onClose();
      } else {
        setError(response.error || 'Failed to delete script');
      }
    } catch (err) {
      setError('Error deleting script: ' + (err.message || 'Unknown error'));
    } finally {
      setSaving(false);
    }
  };

  /**
   * Handle cancel
   */
  const handleCancel = () => {
    onClose();
  };

  return (
    <div className="edit-script-dialog-overlay" data-testid="edit-script-dialog-overlay">
      <div className="edit-script-dialog" data-testid="edit-script-dialog">
        <div className="edit-script-dialog-header">
          <h2>{isEditMode ? 'Edit Script' : 'Add Script'}</h2>
        </div>

        <div className="edit-script-dialog-content">
          {error && (
            <div className="edit-script-dialog-error" data-testid="error-message">
              {error}
            </div>
          )}

          {validationError && (
            <div className="edit-script-dialog-error" data-testid="validation-error">
              {validationError}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="scriptName">Script Name *</label>
            <input
              id="scriptName"
              type="text"
              value={scriptName}
              onChange={(e) => {
                setScriptName(e.target.value);
                setValidationError('');
              }}
              disabled={saving}
              placeholder="Enter script name"
            />
          </div>

          <div className="form-group">
            <label htmlFor="scriptFileName">Script File Name</label>
            <input
              id="scriptFileName"
              type="text"
              value={scriptFileName}
              onChange={(e) => setScriptFileName(e.target.value)}
              disabled={saving}
              placeholder="e.g., process-images.py"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="runTime">Run Time (Daily)</label>
              <input
                id="runTime"
                type="time"
                value={runTime}
                onChange={handleRunTimeChange}
                disabled={saving}
              />
              <small className="form-help">HH:MM format (24-hour)</small>
            </div>

            <div className="form-group">
              <label htmlFor="periodicity">Periodicity</label>
              <select
                id="periodicity"
                value={periodicityMinutes}
                onChange={handlePeriodicityChange}
                disabled={saving}
              >
                <option value="">None</option>
                <option value="1">1 minute</option>
                <option value="5">5 minutes</option>
                <option value="10">10 minutes</option>
                <option value="60">1 hour</option>
                <option value="120">2 hours</option>
                <option value="360">6 hours</option>
                <option value="1440">1 day</option>
              </select>
              <small className="form-help">Run Time and Periodicity are mutually exclusive</small>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="fileExtension">File Extension</label>
            <input
              id="fileExtension"
              type="text"
              value={fileExtension}
              onChange={(e) => setFileExtension(e.target.value)}
              disabled={saving}
              placeholder="e.g., .jpg, .png"
            />
          </div>

          <div className="form-group">
            <label htmlFor="scriptContents">Script Contents</label>
            <textarea
              id="scriptContents"
              value={scriptContents}
              onChange={(e) => setScriptContents(e.target.value)}
              disabled={saving}
              placeholder="Enter script code here..."
              rows={12}
            />
          </div>
        </div>

        <div className="edit-script-dialog-footer">
          <div className="footer-left">
            {isEditMode && (
              <button
                onClick={handleDelete}
                disabled={saving}
                className="delete-button"
                data-testid="delete-button"
              >
                Delete
              </button>
            )}
          </div>
          <div className="footer-right">
            <button
              onClick={handleCancel}
              disabled={saving}
              className="cancel-button"
              data-testid="cancel-button"
            >
              Cancel
            </button>
            <button
              onClick={handleSave}
              disabled={saving}
              className="save-button"
              data-testid="save-button"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditScriptDialog;
