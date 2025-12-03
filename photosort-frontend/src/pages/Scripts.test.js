/**
 * Scripts Page Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Scripts from './Scripts';
import { mockScripts, mockApiResponse } from '../test-utils/mockData';
import scriptService from '../services/scriptService';

// Mock the script service
jest.mock('../services/scriptService');

// Mock alert
global.alert = jest.fn();

describe('Scripts Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderScriptsPage = () => {
    return render(
      <BrowserRouter>
        <Scripts />
      </BrowserRouter>
    );
  };

  it('renders page title and subtitle', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Script Management')).toBeInTheDocument();
    });

    expect(
      screen.getByText('Manage automated scripts for photo processing')
    ).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    scriptService.getAllScripts.mockImplementation(() => new Promise(() => {}));

    renderScriptsPage();

    expect(screen.getByText('Loading scripts...')).toBeInTheDocument();
  });

  it('displays scripts after loading', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Photo Resize Script')).toBeInTheDocument();
    });

    expect(screen.getByText('Metadata Extractor')).toBeInTheDocument();
    expect(screen.getByText('resize_photos.py')).toBeInTheDocument();
    expect(screen.getByText('extract_metadata.sh')).toBeInTheDocument();
  });

  it('displays error message on fetch failure', async () => {
    scriptService.getAllScripts.mockRejectedValue(new Error('Network error'));

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText(/Error loading data/)).toBeInTheDocument();
    });
  });

  it('renders SearchControls component', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Quick Search')).toBeInTheDocument();
    });

    expect(screen.getByText('Advanced Search')).toBeInTheDocument();
  });

  it('renders ScriptTable component with scripts', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Photo Resize Script')).toBeInTheDocument();
    });

    // Check for script table column headers (with sort icons)
    expect(screen.getByText(/Script Name/)).toBeInTheDocument();
    expect(screen.getByText(/Script File/)).toBeInTheDocument();
    expect(screen.getByText(/Schedule/)).toBeInTheDocument();
    expect(screen.getByText(/File Extension/)).toBeInTheDocument();
  });

  it('displays results summary with correct count', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Showing 2 of 2 scripts')).toBeInTheDocument();
    });
  });

  it('shows singular "script" for count of 1', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse([mockScripts[0]])
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Showing 1 of 1 script')).toBeInTheDocument();
    });
  });

  it('renders Add Script button', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Add Script')).toBeInTheDocument();
    });
  });

  it('shows alert when Add Script button clicked', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      const addButton = screen.getByText('Add Script');
      fireEvent.click(addButton);
    });

    expect(global.alert).toHaveBeenCalledWith(
      'Add Script functionality will be implemented in Step 12.'
    );
  });

  it('renders Edit button for each script', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Photo Resize Script')).toBeInTheDocument();
    });

    const editButtons = screen.getAllByText('Edit');
    expect(editButtons.length).toBe(2); // One for each script
  });

  it('shows alert when Edit button clicked', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Photo Resize Script')).toBeInTheDocument();
    });

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    expect(global.alert).toHaveBeenCalledWith(
      expect.stringContaining('Edit functionality will be implemented in Step 12')
    );
  });

  it('handles empty script list', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse([])
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('No scripts found')).toBeInTheDocument();
    });
  });

  it('does not show results summary while loading', () => {
    scriptService.getAllScripts.mockImplementation(() => new Promise(() => {}));

    renderScriptsPage();

    expect(screen.queryByText(/Showing/)).not.toBeInTheDocument();
  });

  it('does not show results summary on error', async () => {
    scriptService.getAllScripts.mockRejectedValue(new Error('Network error'));

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText(/Error loading data/)).toBeInTheDocument();
    });

    expect(screen.queryByText(/Showing/)).not.toBeInTheDocument();
  });

  it('displays schedule types correctly', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('Daily at 02:00')).toBeInTheDocument();
    });

    expect(screen.getByText('Every 1 hour')).toBeInTheDocument();
  });

  it('displays file extensions correctly', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(screen.getByText('.jpg')).toBeInTheDocument();
    });

    expect(screen.getByText('.png')).toBeInTheDocument();
  });

  it('uses correct default sort (scriptName, asc)', async () => {
    scriptService.getAllScripts.mockResolvedValue(
      mockApiResponse(mockScripts)
    );

    renderScriptsPage();

    await waitFor(() => {
      expect(scriptService.getAllScripts).toHaveBeenCalledWith(
        expect.objectContaining({
          sortBy: 'scriptName',
          sortDir: 'asc'
        })
      );
    });
  });
});
