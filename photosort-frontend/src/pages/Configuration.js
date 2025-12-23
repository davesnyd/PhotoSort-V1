/**
 * Configuration Page
 * Copyright 2025, David Snyderman
 *
 * Admin-only page for editing system configuration
 */

import React, { useState, useEffect } from 'react';
import configService from '../services/configService';

const Configuration = () => {
  const [config, setConfig] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [rescanning, setRescanning] = useState(false);
  const [rescanResult, setRescanResult] = useState(null);

  // Load configuration on component mount
  useEffect(() => {
    loadConfiguration();
  }, []);

  const loadConfiguration = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await configService.getConfiguration();

      if (response.success) {
        setConfig(response.data);
      } else {
        setError(response.error?.message || 'Failed to load configuration');
      }
    } catch (err) {
      setError('Error loading configuration: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (section, field, value) => {
    setConfig(prev => ({
      ...prev,
      [section]: {
        ...prev[section],
        [field]: value
      }
    }));
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleSave();
    }
  };

  const handleRescan = async () => {
    try {
      setRescanning(true);
      setRescanResult(null);
      setError(null);

      const response = await configService.triggerRescan();

      if (response.success) {
        setRescanResult({
          success: true,
          message: `Rescan completed! ${response.data.photosProcessed} photo(s) processed.`
        });

        // Hide success message after 5 seconds
        setTimeout(() => setRescanResult(null), 5000);
      } else {
        setError(response.error?.message || 'Rescan failed');
      }
    } catch (err) {
      setError('Error during rescan: ' + err.message);
    } finally {
      setRescanning(false);
    }
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setSaveSuccess(false);
      setError(null);

      const response = await configService.updateConfiguration(config);

      if (response.success) {
        setSaveSuccess(true);
        // Reload configuration to show redacted passwords
        await loadConfiguration();

        // Hide success message after 3 seconds
        setTimeout(() => setSaveSuccess(false), 3000);
      } else {
        setError(response.error?.message || 'Failed to save configuration');
      }
    } catch (err) {
      setError('Error saving configuration: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>Loading configuration...</div>
      </div>
    );
  }

  if (error && !config) {
    return (
      <div style={styles.container}>
        <div style={styles.error}>Error loading configuration: {error}</div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>System Configuration</h1>
      <p style={styles.subtitle}>Manage system settings and credentials</p>

      {error && <div style={styles.error}>Error: {error}</div>}
      {saveSuccess && <div style={styles.success}>Configuration saved successfully!</div>}
      {rescanResult && <div style={styles.success}>{rescanResult.message}</div>}

      <div style={styles.form}>
        {/* Database Configuration Section */}
        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>Database Configuration</h2>
          <div style={styles.field}>
            <label style={styles.label}>Database URI:</label>
            <input
              type="text"
              value={config?.database?.uri || ''}
              onChange={(e) => handleInputChange('database', 'uri', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Username:</label>
            <input
              type="text"
              value={config?.database?.username || ''}
              onChange={(e) => handleInputChange('database', 'username', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Password:</label>
            <input
              type="password"
              value={config?.database?.password || ''}
              onChange={(e) => handleInputChange('database', 'password', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
              placeholder="Leave as ******** to keep unchanged"
            />
          </div>
        </div>

        {/* Git Configuration Section */}
        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>Git Configuration</h2>
          <div style={styles.field}>
            <label style={styles.label}>Repository Path:</label>
            <input
              type="text"
              value={config?.git?.repoPath || ''}
              onChange={(e) => handleInputChange('git', 'repoPath', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Repository URL:</label>
            <input
              type="text"
              value={config?.git?.url || ''}
              onChange={(e) => handleInputChange('git', 'url', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Username:</label>
            <input
              type="text"
              value={config?.git?.username || ''}
              onChange={(e) => handleInputChange('git', 'username', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Access Token:</label>
            <input
              type="password"
              value={config?.git?.token || ''}
              onChange={(e) => handleInputChange('git', 'token', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
              placeholder="Leave as ******** to keep unchanged"
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Poll Interval (minutes):</label>
            <input
              type="number"
              value={config?.git?.pollIntervalMinutes || 5}
              onChange={(e) => handleInputChange('git', 'pollIntervalMinutes', parseInt(e.target.value))}
              onKeyDown={handleKeyDown}
              style={styles.input}
              min="1"
            />
          </div>
          <div style={styles.rescanContainer}>
            <button
              onClick={handleRescan}
              disabled={rescanning}
              style={{
                ...styles.rescanButton,
                ...(rescanning ? styles.rescanButtonDisabled : {})
              }}
            >
              {rescanning ? 'Rescanning...' : 'Rescan Photos Now'}
            </button>
            <p style={styles.rescanHelp}>
              Forces a full rescan of all photos in the configured repository directory.
            </p>
          </div>
        </div>

        {/* OAuth Configuration Section */}
        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>OAuth Configuration</h2>
          <div style={styles.field}>
            <label style={styles.label}>Client ID:</label>
            <input
              type="text"
              value={config?.oauth?.clientId || ''}
              onChange={(e) => handleInputChange('oauth', 'clientId', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Client Secret:</label>
            <input
              type="password"
              value={config?.oauth?.clientSecret || ''}
              onChange={(e) => handleInputChange('oauth', 'clientSecret', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
              placeholder="Leave as ******** to keep unchanged"
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Redirect URI:</label>
            <input
              type="text"
              value={config?.oauth?.redirectUri || ''}
              onChange={(e) => handleInputChange('oauth', 'redirectUri', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
        </div>

        {/* STAG Configuration Section */}
        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>STAG Configuration</h2>
          <div style={styles.field}>
            <label style={styles.label}>Script Path:</label>
            <input
              type="text"
              value={config?.stag?.scriptPath || ''}
              onChange={(e) => handleInputChange('stag', 'scriptPath', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
          <div style={styles.field}>
            <label style={styles.label}>Python Executable:</label>
            <input
              type="text"
              value={config?.stag?.pythonExecutable || ''}
              onChange={(e) => handleInputChange('stag', 'pythonExecutable', e.target.value)}
              onKeyDown={handleKeyDown}
              style={styles.input}
            />
          </div>
        </div>

        {/* Save Button */}
        <div style={styles.buttonContainer}>
          <button
            onClick={handleSave}
            disabled={saving}
            style={{
              ...styles.saveButton,
              ...(saving ? styles.saveButtonDisabled : {})
            }}
          >
            {saving ? 'Saving...' : 'Save Configuration'}
          </button>
        </div>
      </div>
    </div>
  );
};

// Styles matching the application's color scheme
const styles = {
  container: {
    padding: '20px',
    maxWidth: '1200px',
    margin: '0 auto'
  },
  title: {
    color: '#800020', // Burgundy
    marginBottom: '10px',
    fontSize: '28px'
  },
  subtitle: {
    color: '#000080', // Navy
    marginBottom: '20px',
    fontSize: '16px'
  },
  loading: {
    textAlign: 'center',
    padding: '40px',
    color: '#000080'
  },
  error: {
    backgroundColor: '#ffebee',
    color: '#c62828',
    padding: '12px',
    borderRadius: '4px',
    marginBottom: '20px'
  },
  success: {
    backgroundColor: '#e8f5e9',
    color: '#2e7d32',
    padding: '12px',
    borderRadius: '4px',
    marginBottom: '20px'
  },
  form: {
    backgroundColor: '#FFFDD0', // Cream
    padding: '20px',
    borderRadius: '8px'
  },
  section: {
    marginBottom: '30px',
    paddingBottom: '20px',
    borderBottom: '2px solid #800020'
  },
  sectionTitle: {
    color: '#000080', // Navy
    marginBottom: '15px',
    fontSize: '20px'
  },
  field: {
    marginBottom: '15px'
  },
  label: {
    display: 'block',
    marginBottom: '5px',
    color: '#000080',
    fontWeight: 'bold',
    fontSize: '14px'
  },
  input: {
    width: '100%',
    padding: '10px',
    fontSize: '14px',
    border: '2px solid #000080',
    borderRadius: '4px',
    boxSizing: 'border-box'
  },
  buttonContainer: {
    textAlign: 'center',
    marginTop: '20px'
  },
  saveButton: {
    backgroundColor: '#800020', // Burgundy
    color: '#FFFDD0', // Cream
    padding: '12px 30px',
    fontSize: '16px',
    fontWeight: 'bold',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer'
  },
  saveButtonDisabled: {
    opacity: 0.6,
    cursor: 'not-allowed'
  },
  rescanContainer: {
    marginTop: '20px',
    paddingTop: '15px',
    borderTop: '1px dashed #000080'
  },
  rescanButton: {
    backgroundColor: '#000080', // Navy
    color: '#FFFDD0', // Cream
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer'
  },
  rescanButtonDisabled: {
    opacity: 0.6,
    cursor: 'not-allowed'
  },
  rescanHelp: {
    color: '#666',
    fontSize: '12px',
    marginTop: '8px',
    marginBottom: '0'
  }
};

export default Configuration;
