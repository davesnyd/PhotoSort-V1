/**
 * User Access Dialog Tests
 * Copyright 2025, David Snyderman
 *
 * Automated tests for UserAccessDialog component
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import UserAccessDialog from './UserAccessDialog';

// Mock the services
jest.mock('../services/photoService', () => ({
  __esModule: true,
  default: {
    getPhotoPermissions: jest.fn(),
    updatePhotoPermissions: jest.fn()
  }
}));

jest.mock('../services/userService', () => ({
  __esModule: true,
  default: {
    getAllUsers: jest.fn()
  }
}));

import photoService from '../services/photoService';
import userService from '../services/userService';

describe('UserAccessDialog Component', () => {
  const mockOnClose = jest.fn();
  const mockOnSave = jest.fn();

  const mockUsers = [
    { userId: 1, displayName: 'User One', email: 'user1@test.com' },
    { userId: 2, displayName: 'User Two', email: 'user2@test.com' },
    { userId: 3, displayName: 'User Three', email: 'user3@test.com' }
  ];

  beforeEach(() => {
    jest.clearAllMocks();

    // Setup default mock responses
    userService.getAllUsers.mockResolvedValue({
      success: true,
      data: mockUsers
    });

    photoService.getPhotoPermissions.mockResolvedValue({
      success: true,
      data: [1] // User 1 has access by default
    });
  });

  /**
   * Test Case 1: Component renders with photo filename
   */
  test('renders dialog with photo filename', async () => {
    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    expect(screen.getByText(/Manage Photo Access - test-photo.jpg/i)).toBeInTheDocument();
  });

  /**
   * Test Case 2: Loading state displays while fetching data
   */
  test('displays loading state initially', () => {
    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    expect(screen.getByTestId('loading-indicator')).toBeInTheDocument();
  });

  /**
   * Test Case 3: Displays all users after loading
   */
  test('displays all users after loading', async () => {
    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('User One')).toBeInTheDocument();
      expect(screen.getByText('user1@test.com')).toBeInTheDocument();
      expect(screen.getByText('User Two')).toBeInTheDocument();
      expect(screen.getByText('user2@test.com')).toBeInTheDocument();
      expect(screen.getByText('User Three')).toBeInTheDocument();
      expect(screen.getByText('user3@test.com')).toBeInTheDocument();
    });
  });

  /**
   * Test Case 4: Users with current access have checkboxes checked
   */
  test('shows checked checkboxes for users with access', async () => {
    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      const checkbox1 = screen.getByTestId('checkbox-1');
      const checkbox2 = screen.getByTestId('checkbox-2');

      expect(checkbox1).toBeChecked();
      expect(checkbox2).not.toBeChecked();
    });
  });

  /**
   * Test Case 5: Can check/uncheck user permissions
   */
  test('can toggle user permissions', async () => {
    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('checkbox-1')).toBeChecked();
    });

    // Uncheck user 1
    const checkbox1 = screen.getByTestId('checkbox-1');
    fireEvent.click(checkbox1);
    expect(checkbox1).not.toBeChecked();

    // Check user 2
    const checkbox2 = screen.getByTestId('checkbox-2');
    fireEvent.click(checkbox2);
    expect(checkbox2).toBeChecked();
  });

  /**
   * Test Case 6: Save button calls updatePhotoPermissions with correct data
   */
  test('save button updates permissions and closes dialog', async () => {
    photoService.updatePhotoPermissions.mockResolvedValue({
      success: true,
      data: 'Permissions updated successfully'
    });

    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    // Wait for data to load
    await waitFor(() => {
      expect(screen.getByTestId('checkbox-1')).toBeChecked();
    });

    // Check user 2
    const checkbox2 = screen.getByTestId('checkbox-2');
    fireEvent.click(checkbox2);

    // Click save
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(photoService.updatePhotoPermissions).toHaveBeenCalledWith(123, [1, 2]);
      expect(mockOnSave).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  /**
   * Test Case 7: Cancel button closes dialog without saving
   */
  test('cancel button closes dialog without saving', async () => {
    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('checkbox-1')).toBeChecked();
    });

    // Make a change
    const checkbox2 = screen.getByTestId('checkbox-2');
    fireEvent.click(checkbox2);

    // Click cancel
    const cancelButton = screen.getByTestId('cancel-button');
    fireEvent.click(cancelButton);

    expect(photoService.updatePhotoPermissions).not.toHaveBeenCalled();
    expect(mockOnSave).not.toHaveBeenCalled();
    expect(mockOnClose).toHaveBeenCalled();
  });

  /**
   * Test Case 8: Displays error when users fail to load
   */
  test('displays error when users fail to load', async () => {
    userService.getAllUsers.mockResolvedValue({
      success: false,
      error: 'Failed to load users'
    });

    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
      expect(screen.getByText(/Failed to load users/i)).toBeInTheDocument();
    });
  });

  /**
   * Test Case 9: Displays error when permissions fail to load
   */
  test('displays error when permissions fail to load', async () => {
    photoService.getPhotoPermissions.mockResolvedValue({
      success: false,
      error: 'Failed to load permissions'
    });

    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
      expect(screen.getByText(/Failed to load permissions/i)).toBeInTheDocument();
    });
  });

  /**
   * Test Case 10: Displays error when save fails
   */
  test('displays error when save fails', async () => {
    photoService.updatePhotoPermissions.mockResolvedValue({
      success: false,
      error: 'Failed to save permissions'
    });

    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('checkbox-1')).toBeChecked();
    });

    // Click save
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
      expect(screen.getByText(/Failed to save permissions/i)).toBeInTheDocument();
    });

    expect(mockOnClose).not.toHaveBeenCalled();
  });

  /**
   * Test Case 11: Displays "no users" message when user list is empty
   */
  test('displays no users message when list is empty', async () => {
    userService.getAllUsers.mockResolvedValue({
      success: true,
      data: []
    });

    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('no-users-message')).toBeInTheDocument();
      expect(screen.getByText(/No users available/i)).toBeInTheDocument();
    });
  });

  /**
   * Test Case 12: Save button shows "Saving..." when in progress
   */
  test('save button shows saving state', async () => {
    let resolveUpdate;
    photoService.updatePhotoPermissions.mockReturnValue(
      new Promise((resolve) => {
        resolveUpdate = resolve;
      })
    );

    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('checkbox-1')).toBeChecked();
    });

    // Click save
    const saveButton = screen.getByTestId('save-button');
    fireEvent.click(saveButton);

    // Check that button shows "Saving..."
    await waitFor(() => {
      expect(screen.getByText('Saving...')).toBeInTheDocument();
    });

    // Resolve the promise
    resolveUpdate({ success: true, data: 'Permissions updated successfully' });

    await waitFor(() => {
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  /**
   * Test Case 13: Buttons are disabled while saving
   */
  test('buttons are disabled while saving', async () => {
    let resolveUpdate;
    photoService.updatePhotoPermissions.mockReturnValue(
      new Promise((resolve) => {
        resolveUpdate = resolve;
      })
    );

    render(
      <UserAccessDialog
        photoId={123}
        photoFilename="test-photo.jpg"
        onClose={mockOnClose}
        onSave={mockOnSave}
      />
    );

    await waitFor(() => {
      expect(screen.getByTestId('checkbox-1')).toBeChecked();
    });

    const saveButton = screen.getByTestId('save-button');
    const cancelButton = screen.getByTestId('cancel-button');

    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(saveButton).toBeDisabled();
      expect(cancelButton).toBeDisabled();
    });

    resolveUpdate({ success: true, data: 'Permissions updated successfully' });
  });
});
