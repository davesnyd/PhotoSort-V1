/**
 * Configuration Page Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Configuration from './Configuration';
import configService from '../services/configService';

// Mock the config service
jest.mock('../services/configService');

// Mock alert
global.alert = jest.fn();

describe('Configuration Page', () => {
  const mockConfigData = {
    success: true,
    data: {
      database: {
        uri: 'jdbc:postgresql://localhost:5432/PhotoSortData',
        username: 'postgres',
        password: '********'
      },
      git: {
        repoPath: '/path/to/repo',
        url: 'https://github.com/user/repo.git',
        username: 'gituser',
        token: '********',
        pollIntervalMinutes: 5
      },
      oauth: {
        clientId: 'test-client-id',
        clientSecret: '********',
        redirectUri: 'http://localhost:8080/oauth2/callback'
      },
      stag: {
        scriptPath: './stag-main/stag.py',
        pythonExecutable: 'python3'
      }
    }
  };

  beforeEach(() => {
    jest.clearAllMocks();
    configService.getConfiguration.mockResolvedValue(mockConfigData);
  });

  const renderConfigurationPage = () => {
    return render(
      <BrowserRouter>
        <Configuration />
      </BrowserRouter>
    );
  };

  /**
   * Test Case 1: Verify page renders with title
   */
  it('renders page title', async () => {
    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByText('System Configuration')).toBeInTheDocument();
    });
  });

  /**
   * Test Case 2: Verify configuration loads correctly with passwords redacted
   */
  it('loads and displays configuration with redacted passwords', async () => {
    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('jdbc:postgresql://localhost:5432/PhotoSortData')).toBeInTheDocument();
    });

    expect(screen.getByDisplayValue('postgres')).toBeInTheDocument();
    // Multiple password fields will have "********"
    const passwordFields = screen.getAllByDisplayValue('********');
    expect(passwordFields.length).toBeGreaterThan(0);
    expect(screen.getByDisplayValue('/path/to/repo')).toBeInTheDocument();
    expect(screen.getByDisplayValue('https://github.com/user/repo.git')).toBeInTheDocument();
  });

  /**
   * Test Case 3: Verify all configuration sections are displayed
   */
  it('displays all configuration sections', async () => {
    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByText(/Database Configuration/)).toBeInTheDocument();
    });

    expect(screen.getByText(/Git Configuration/)).toBeInTheDocument();
    expect(screen.getByText(/OAuth Configuration/)).toBeInTheDocument();
    expect(screen.getByText(/STAG Configuration/)).toBeInTheDocument();
  });

  /**
   * Test Case 4: Verify user can update database URI
   */
  it('allows user to update database URI', async () => {
    configService.updateConfiguration.mockResolvedValue({
      success: true,
      data: { ...mockConfigData.data }
    });

    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('jdbc:postgresql://localhost:5432/PhotoSortData')).toBeInTheDocument();
    });

    const uriInput = screen.getByDisplayValue('jdbc:postgresql://localhost:5432/PhotoSortData');
    fireEvent.change(uriInput, {
      target: { value: 'jdbc:postgresql://newhost:5432/PhotoSortData' }
    });

    expect(uriInput.value).toBe('jdbc:postgresql://newhost:5432/PhotoSortData');
  });

  /**
   * Test Case 5: Verify user can update Git settings
   */
  it('allows user to update Git poll interval', async () => {
    configService.updateConfiguration.mockResolvedValue({
      success: true,
      data: { ...mockConfigData.data }
    });

    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('5')).toBeInTheDocument();
    });

    const pollIntervalInput = screen.getByDisplayValue('5');
    fireEvent.change(pollIntervalInput, { target: { value: '10' } });

    expect(pollIntervalInput.value).toBe('10');
  });

  /**
   * Test Case 6: Verify Save button exists and is clickable
   */
  it('displays save configuration button', async () => {
    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Save Configuration/i })).toBeInTheDocument();
    });
  });

  /**
   * Test Case 7: Verify save configuration calls service
   */
  it('calls updateConfiguration when save button is clicked', async () => {
    configService.updateConfiguration.mockResolvedValue({
      success: true,
      data: { ...mockConfigData.data }
    });

    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Save Configuration/i })).toBeInTheDocument();
    });

    const saveButton = screen.getByRole('button', { name: /Save Configuration/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(configService.updateConfiguration).toHaveBeenCalled();
    });
  });

  /**
   * Test Case 8: Verify error message displayed on load failure
   */
  it('displays error message when configuration fails to load', async () => {
    configService.getConfiguration.mockRejectedValue(new Error('Network error'));

    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByText(/Error loading configuration/i)).toBeInTheDocument();
    });
  });

  /**
   * Test Case 9: Verify error handling on save failure
   */
  it('displays error message when save fails', async () => {
    configService.getConfiguration.mockResolvedValue(mockConfigData);
    configService.updateConfiguration.mockRejectedValue(new Error('Invalid configuration'));

    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Save Configuration/i })).toBeInTheDocument();
    });

    const saveButton = screen.getByRole('button', { name: /Save Configuration/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(screen.getByText(/Error saving configuration/i)).toBeInTheDocument();
    });
  });

  /**
   * Test Case 10: Verify password fields are displayed as password inputs
   */
  it('renders password fields with type password', async () => {
    renderConfigurationPage();

    await waitFor(() => {
      const passwordInputs = screen.getAllByDisplayValue('********');
      expect(passwordInputs.length).toBeGreaterThan(0);
      // Check that at least one is a password input type
      expect(passwordInputs[0].type).toBe('password');
    });
  });

  /**
   * Test Case 11: Verify unchanged password fields remain as asterisks
   */
  it('keeps password fields as asterisks when not changed', async () => {
    configService.updateConfiguration.mockResolvedValue({
      success: true,
      data: { ...mockConfigData.data }
    });

    renderConfigurationPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Save Configuration/i })).toBeInTheDocument();
    });

    // Don't change password fields, just click save
    const saveButton = screen.getByRole('button', { name: /Save Configuration/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(configService.updateConfiguration).toHaveBeenCalledWith(
        expect.objectContaining({
          database: expect.objectContaining({
            password: '********'
          })
        })
      );
    });
  });

  /**
   * Test Case 12: Verify loading state is displayed
   */
  it('shows loading state while fetching configuration', () => {
    configService.getConfiguration.mockImplementation(() => new Promise(() => {}));

    renderConfigurationPage();

    expect(screen.getByText(/Loading configuration/i)).toBeInTheDocument();
  });
});
