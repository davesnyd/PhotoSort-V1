/**
 * Users Page Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Users from './Users';
import { mockUsers, mockApiResponse, mockPagedResponse } from '../test-utils/mockData';
import userService from '../services/userService';

// Mock the user service
jest.mock('../services/userService');

// Mock alert
global.alert = jest.fn();

describe('Users Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderUsersPage = () => {
    return render(
      <BrowserRouter>
        <Users />
      </BrowserRouter>
    );
  };

  it('renders page title and subtitle', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('User Management')).toBeInTheDocument();
    });

    expect(
      screen.getByText('Manage users, view photo counts, and update permissions')
    ).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    userService.getUsers.mockImplementation(() => new Promise(() => {}));

    renderUsersPage();

    expect(screen.getByText('Loading users...')).toBeInTheDocument();
  });

  it('displays users after loading', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    });

    expect(screen.getByText('admin@example.com')).toBeInTheDocument();
    expect(screen.getByText('User One')).toBeInTheDocument();
    expect(screen.getByText('Admin User')).toBeInTheDocument();
  });

  it('displays error message on fetch failure', async () => {
    userService.getUsers.mockRejectedValue(new Error('Network error'));

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText(/Error loading data/)).toBeInTheDocument();
    });
  });

  it('renders SearchControls component', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('Quick Search')).toBeInTheDocument();
    });

    expect(screen.getByText('Advanced Search')).toBeInTheDocument();
  });

  it('renders UserTable component with users', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    });

    // Check for user table column headers (with sort icons)
    expect(screen.getByText(/Email/)).toBeInTheDocument();
    expect(screen.getByText(/Display Name/)).toBeInTheDocument();
    expect(screen.getByText(/Type/)).toBeInTheDocument();
  });

  it('displays results summary with correct count', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('Showing 2 of 2 users')).toBeInTheDocument();
    });
  });

  it('shows singular "user" for count of 1', async () => {
    const singleUser = {
      content: [mockUsers[0]],
      page: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 1
    };

    userService.getUsers.mockResolvedValue(mockApiResponse(singleUser));

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('Showing 1 of 1 user')).toBeInTheDocument();
    });
  });

  it('renders PaginationControls when multiple pages', async () => {
    const pagedData = {
      content: mockUsers,
      page: 0,
      pageSize: 10,
      totalPages: 3,
      totalElements: 25
    };

    userService.getUsers.mockResolvedValue(mockApiResponse(pagedData));

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
  });

  it('does not render pagination with single page', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    });

    // Pagination should not be visible
    expect(screen.queryByText(/Page 1 of 1/)).not.toBeInTheDocument();
  });

  it('uses correct default sort (email, asc)', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(userService.getUsers).toHaveBeenCalledWith(
        expect.objectContaining({
          sortBy: 'email',
          sortDir: 'asc'
        })
      );
    });
  });

  it('handles user type change successfully', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );
    userService.updateUserType.mockResolvedValue(
      mockApiResponse({ userId: 1, userType: 'ADMIN' })
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    });

    // Click Edit on first user
    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    // Change user type
    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'ADMIN' } });

    // Click Save
    const saveButton = screen.getByText('Save');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(userService.updateUserType).toHaveBeenCalledWith(1, 'ADMIN');
    });

    // Should refresh the data
    await waitFor(() => {
      expect(userService.getUsers).toHaveBeenCalledTimes(2); // Initial + refresh
    });
  });

  it('shows alert on user type change error', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );
    userService.updateUserType.mockResolvedValue({
      success: false,
      error: { message: 'Permission denied' }
    });

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    });

    // Click Edit and Save
    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    const saveButton = screen.getByText('Save');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(global.alert).toHaveBeenCalledWith('Error: Permission denied');
    });
  });

  it('handles user type change network error', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );
    userService.updateUserType.mockRejectedValue({
      response: { data: { error: { message: 'Network error' } } }
    });

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    });

    // Click Edit and Save
    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    const saveButton = screen.getByText('Save');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(global.alert).toHaveBeenCalledWith('Network error');
    });
  });

  it('does not show results summary while loading', () => {
    userService.getUsers.mockImplementation(() => new Promise(() => {}));

    renderUsersPage();

    expect(screen.queryByText(/Showing/)).not.toBeInTheDocument();
  });

  it('does not show results summary on error', async () => {
    userService.getUsers.mockRejectedValue(new Error('Network error'));

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText(/Error loading data/)).toBeInTheDocument();
    });

    expect(screen.queryByText(/Showing/)).not.toBeInTheDocument();
  });

  it('handles empty user list', async () => {
    const emptyData = {
      content: [],
      page: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0
    };

    userService.getUsers.mockResolvedValue(mockApiResponse(emptyData));

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('No users found')).toBeInTheDocument();
    });
  });

  it('renders Edit and View Images buttons for each user', async () => {
    userService.getUsers.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockUsers))
    );

    renderUsersPage();

    await waitFor(() => {
      expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    });

    const editButtons = screen.getAllByText('Edit');
    const viewButtons = screen.getAllByText('View Images');

    expect(editButtons.length).toBe(2); // One for each user
    expect(viewButtons.length).toBe(2);
  });
});
