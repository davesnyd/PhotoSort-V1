/**
 * Edit Script Dialog Tests
 * Copyright 2025, David Snyderman
 *
 * Automated tests for EditScriptDialog component
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import EditScriptDialog from './EditScriptDialog';

// Mock the scriptService
jest.mock('../services/scriptService', () => ({
  __esModule: true,
  default: {
    createScript: jest.fn(),
    updateScript: jest.fn(),
    deleteScript: jest.fn()
  }
}));

import scriptService from '../services/scriptService';

describe('EditScriptDialog Component', () => {
  const mockOnClose = jest.fn();
  const mockOnSave = jest.fn();

  const mockExistingScript = {
    scriptId: 1,
    scriptName: 'Test Script',
    scriptFileName: 'test-script.py',
    runTime: '08:00:00',
    periodicityMinutes: null,
    fileExtension: '.jpg',
    scriptContents: 'print("Hello World")'
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  /**
   * Test Case 1: Dialog renders with empty fields for new script
   */
  test('renders dialog with empty fields for new script', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    expect(screen.getByText(/Add Script/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Script Name/i)).toHaveValue('');
    expect(screen.getByLabelText(/Run Time/i)).toHaveValue('');
    expect(screen.getByLabelText(/Periodicity/i)).toHaveValue('');
    expect(screen.getByLabelText(/File Extension/i)).toHaveValue('');
    expect(screen.getByLabelText(/Script Contents/i)).toHaveValue('');
  });

  /**
   * Test Case 2: Dialog renders with populated fields for editing
   */
  test('renders dialog with populated fields for editing', () => {
    render(
      <EditScriptDialog
        script={mockExistingScript}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    expect(screen.getByText(/Edit Script/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Script Name/i)).toHaveValue('Test Script');
    expect(screen.getByLabelText(/Run Time/i)).toHaveValue('08:00');
    expect(screen.getByLabelText(/File Extension/i)).toHaveValue('.jpg');
    expect(screen.getByLabelText(/Script Contents/i)).toHaveValue('print("Hello World")');
  });

  /**
   * Test Case 3: User can input script name
   */
  test('user can input script name', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const nameInput = screen.getByLabelText(/Script Name/i);
    fireEvent.change(nameInput, { target: { value: 'New Script' } });
    expect(nameInput).toHaveValue('New Script');
  });

  /**
   * Test Case 4: User can input run time
   */
  test('user can input run time', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const runTimeInput = screen.getByLabelText(/Run Time/i);
    fireEvent.change(runTimeInput, { target: { value: '14:30' } });
    expect(runTimeInput).toHaveValue('14:30');
  });

  /**
   * Test Case 5: User can select periodicity
   */
  test('user can select periodicity', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const periodicitySelect = screen.getByLabelText(/Periodicity/i);
    fireEvent.change(periodicitySelect, { target: { value: '60' } });
    expect(periodicitySelect).toHaveValue('60');
  });

  /**
   * Test Case 6: Clears run time when periodicity is selected
   */
  test('clears run time when periodicity is selected', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const runTimeInput = screen.getByLabelText(/Run Time/i);
    const periodicitySelect = screen.getByLabelText(/Periodicity/i);

    // Set run time
    fireEvent.change(runTimeInput, { target: { value: '14:30' } });
    expect(runTimeInput).toHaveValue('14:30');

    // Set periodicity
    fireEvent.change(periodicitySelect, { target: { value: '60' } });
    expect(runTimeInput).toHaveValue('');
  });

  /**
   * Test Case 7: Clears periodicity when run time is entered
   */
  test('clears periodicity when run time is entered', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const runTimeInput = screen.getByLabelText(/Run Time/i);
    const periodicitySelect = screen.getByLabelText(/Periodicity/i);

    // Set periodicity
    fireEvent.change(periodicitySelect, { target: { value: '60' } });
    expect(periodicitySelect).toHaveValue('60');

    // Set run time
    fireEvent.change(runTimeInput, { target: { value: '14:30' } });
    expect(periodicitySelect).toHaveValue('');
  });

  /**
   * Test Case 8: User can input file extension
   */
  test('user can input file extension', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const extensionInput = screen.getByLabelText(/File Extension/i);
    fireEvent.change(extensionInput, { target: { value: '.png' } });
    expect(extensionInput).toHaveValue('.png');
  });

  /**
   * Test Case 9: User can input script contents
   */
  test('user can input script contents', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const contentsTextarea = screen.getByLabelText(/Script Contents/i);
    const scriptCode = 'import os\nprint("test")';
    fireEvent.change(contentsTextarea, { target: { value: scriptCode } });
    expect(contentsTextarea).toHaveValue(scriptCode);
  });

  /**
   * Test Case 10: Save button creates new script
   */
  test('save button creates new script', async () => {
    scriptService.createScript.mockResolvedValue({
      success: true,
      data: { scriptId: 2, scriptName: 'New Script' }
    });

    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Fill in form
    fireEvent.change(screen.getByLabelText(/Script Name/i), { target: { value: 'New Script' } });
    fireEvent.change(screen.getByLabelText(/Run Time/i), { target: { value: '09:00' } });
    fireEvent.change(screen.getByLabelText(/File Extension/i), { target: { value: '.jpg' } });
    fireEvent.change(screen.getByLabelText(/Script Contents/i), { target: { value: 'test code' } });

    // Click save
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(scriptService.createScript).toHaveBeenCalledWith(
        expect.objectContaining({
          scriptName: 'New Script',
          runTime: '09:00:00',
          fileExtension: '.jpg',
          scriptContents: 'test code'
        })
      );
      expect(mockOnSave).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  /**
   * Test Case 11: Save button updates existing script
   */
  test('save button updates existing script', async () => {
    scriptService.updateScript.mockResolvedValue({
      success: true,
      data: { scriptId: 1, scriptName: 'Updated Script' }
    });

    render(
      <EditScriptDialog
        script={mockExistingScript}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Update script name
    fireEvent.change(screen.getByLabelText(/Script Name/i), { target: { value: 'Updated Script' } });

    // Click save
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(scriptService.updateScript).toHaveBeenCalledWith(
        1,
        expect.objectContaining({
          scriptName: 'Updated Script'
        })
      );
      expect(mockOnSave).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  /**
   * Test Case 12: Delete button shows confirmation before deleting
   */
  test('delete button requires confirmation', () => {
    window.confirm = jest.fn(() => false); // User cancels

    render(
      <EditScriptDialog
        script={mockExistingScript}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const deleteButton = screen.getByTestId('delete-button');
    fireEvent.click(deleteButton);

    expect(window.confirm).toHaveBeenCalledWith(
      expect.stringContaining('Test Script')
    );
    expect(scriptService.deleteScript).not.toHaveBeenCalled();
  });

  /**
   * Test Case 13: Delete button deletes script after confirmation
   */
  test('delete button deletes script after confirmation', async () => {
    window.confirm = jest.fn(() => true); // User confirms
    scriptService.deleteScript.mockResolvedValue({
      success: true,
      data: 'Script deleted'
    });

    render(
      <EditScriptDialog
        script={mockExistingScript}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const deleteButton = screen.getByTestId('delete-button');
    fireEvent.click(deleteButton);

    await waitFor(() => {
      expect(scriptService.deleteScript).toHaveBeenCalledWith(1);
      expect(mockOnSave).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  /**
   * Test Case 14: Cancel button closes dialog without saving
   */
  test('cancel button closes dialog without saving', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Make changes
    fireEvent.change(screen.getByLabelText(/Script Name/i), { target: { value: 'New Script' } });

    // Click cancel
    const cancelButton = screen.getByTestId('cancel-button');
    fireEvent.click(cancelButton);

    expect(scriptService.createScript).not.toHaveBeenCalled();
    expect(scriptService.updateScript).not.toHaveBeenCalled();
    expect(mockOnSave).not.toHaveBeenCalled();
    expect(mockOnClose).toHaveBeenCalled();
  });

  /**
   * Test Case 15: Delete button not shown for new scripts
   */
  test('delete button not shown for new scripts', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    expect(screen.queryByTestId('delete-button')).not.toBeInTheDocument();
  });

  /**
   * Test Case 16: Displays error when save fails
   */
  test('displays error when save fails', async () => {
    scriptService.createScript.mockResolvedValue({
      success: false,
      error: 'Failed to save script'
    });

    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Fill in minimal required fields
    fireEvent.change(screen.getByLabelText(/Script Name/i), { target: { value: 'New Script' } });

    // Click save
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
      expect(screen.getByText(/Failed to save script/i)).toBeInTheDocument();
    });

    expect(mockOnClose).not.toHaveBeenCalled();
  });

  /**
   * Test Case 17: Save button shows "Saving..." when in progress
   */
  test('save button shows saving state', async () => {
    let resolveCreate;
    scriptService.createScript.mockReturnValue(
      new Promise((resolve) => {
        resolveCreate = resolve;
      })
    );

    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Fill in form
    fireEvent.change(screen.getByLabelText(/Script Name/i), { target: { value: 'New Script' } });

    // Click save
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    // Check that button shows "Saving..."
    await waitFor(() => {
      expect(screen.getByText('Saving...')).toBeInTheDocument();
    });

    // Resolve the promise
    resolveCreate({ success: true, data: { scriptId: 2 } });

    await waitFor(() => {
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  /**
   * Test Case 18: Buttons are disabled while saving
   */
  test('buttons are disabled while saving', async () => {
    let resolveCreate;
    scriptService.createScript.mockReturnValue(
      new Promise((resolve) => {
        resolveCreate = resolve;
      })
    );

    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Fill in form
    fireEvent.change(screen.getByLabelText(/Script Name/i), { target: { value: 'New Script' } });

    const saveButton = screen.getByTestId('save-button');
    const cancelButton = screen.getByTestId('cancel-button');

    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(saveButton).toBeDisabled();
      expect(cancelButton).toBeDisabled();
    });

    resolveCreate({ success: true, data: { scriptId: 2 } });
  });

  /**
   * Test Case 19: Validation prevents saving with empty script name
   */
  test('validation prevents saving with empty script name', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Try to save without script name
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    // Should show validation error
    expect(screen.getByText(/Script name is required/i)).toBeInTheDocument();
    expect(scriptService.createScript).not.toHaveBeenCalled();
  });

  /**
   * Test Case 20: Periodicity dropdown shows correct options
   */
  test('periodicity dropdown shows correct options', () => {
    render(
      <EditScriptDialog
        script={null}
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    const periodicitySelect = screen.getByLabelText(/Periodicity/i);
    const options = Array.from(periodicitySelect.options).map(opt => opt.value);

    expect(options).toContain('');
    expect(options).toContain('1');
    expect(options).toContain('5');
    expect(options).toContain('10');
    expect(options).toContain('60');
    expect(options).toContain('120');
    expect(options).toContain('360');
    expect(options).toContain('1440');
  });
});
