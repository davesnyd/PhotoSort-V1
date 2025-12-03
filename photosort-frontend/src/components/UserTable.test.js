/**
 * UserTable Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import UserTable from './UserTable';
import { mockUsers } from '../test-utils/mockData';

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate
}));

describe('UserTable Component', () => {
  const mockOnSort = jest.fn();
  const mockOnUserTypeChange = jest.fn();
  const mockCurrentSort = { field: 'userId', direction: 'asc' };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderUserTable = (props = {}) => {
    return render(
      <BrowserRouter>
        <UserTable
          users={mockUsers}
          onSortChange={mockOnSort}
          onUserTypeChange={mockOnUserTypeChange}
          currentSort={mockCurrentSort}
          {...props}
        />
      </BrowserRouter>
    );
  };

  it('renders user data in table', () => {
    renderUserTable();

    expect(screen.getByText('user1@example.com')).toBeInTheDocument();
    expect(screen.getByText('admin@example.com')).toBeInTheDocument();
    expect(screen.getByText('User One')).toBeInTheDocument();
    expect(screen.getByText('Admin User')).toBeInTheDocument();
  });

  it('displays user type badges with correct styling', () => {
    renderUserTable();

    const userBadge = screen.getByText('USER');
    expect(userBadge).toHaveClass('user-type-badge', 'user');

    const adminBadge = screen.getByText('ADMIN');
    expect(adminBadge).toHaveClass('user-type-badge', 'admin');
  });

  it('displays photo counts', () => {
    renderUserTable();

    expect(screen.getByText('5')).toBeInTheDocument(); // User One's photo count
    expect(screen.getByText('10')).toBeInTheDocument(); // Admin User's photo count
  });

  it('formats dates correctly', () => {
    renderUserTable();

    // Dates should be formatted (exact format depends on locale) - use getAllByText since multiple dates exist
    expect(screen.getAllByText(/Jan.*2024/)).toHaveLength(2); // Two users with Jan dates
    expect(screen.getAllByText(/Dec.*2024/)).toHaveLength(2); // Last login dates
  });

  it('shows "N/A" for missing display name', () => {
    const usersWithMissingName = [
      { ...mockUsers[0], displayName: null }
    ];

    render(
      <BrowserRouter>
        <UserTable
          users={usersWithMissingName}
          onSortChange={mockOnSort}
          onUserTypeChange={mockOnUserTypeChange}
          currentSort={mockCurrentSort}
        />
      </BrowserRouter>
    );

    expect(screen.getByText('N/A')).toBeInTheDocument();
  });

  it('enters edit mode when Edit button clicked', () => {
    renderUserTable();

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    // Should show Save and Cancel buttons
    expect(screen.getByText('Save')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();

    // Should show dropdown for user type
    const select = screen.getByRole('combobox');
    expect(select).toBeInTheDocument();
    expect(select).toHaveValue('USER');
  });

  it('shows user type dropdown in edit mode with correct options', () => {
    renderUserTable();

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    const select = screen.getByRole('combobox');
    const options = Array.from(select.options).map(opt => opt.value);

    expect(options).toEqual(['USER', 'ADMIN']);
  });

  it('changes user type selection in edit mode', () => {
    renderUserTable();

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'ADMIN' } });

    expect(select).toHaveValue('ADMIN');
  });

  it('calls onUserTypeChange when Save clicked', () => {
    renderUserTable();

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'ADMIN' } });

    const saveButton = screen.getByText('Save');
    fireEvent.click(saveButton);

    expect(mockOnUserTypeChange).toHaveBeenCalledWith(1, 'ADMIN');
  });

  it('exits edit mode after Save clicked', () => {
    renderUserTable();

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    const saveButton = screen.getByText('Save');
    fireEvent.click(saveButton);

    // Should no longer show Save/Cancel buttons
    expect(screen.queryByText('Save')).not.toBeInTheDocument();
    expect(screen.queryByText('Cancel')).not.toBeInTheDocument();

    // Should show Edit button again
    expect(screen.getAllByText('Edit').length).toBe(mockUsers.length);
  });

  it('exits edit mode when Cancel clicked without saving', () => {
    renderUserTable();

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'ADMIN' } });

    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    // Should not call onUserTypeChange
    expect(mockOnUserTypeChange).not.toHaveBeenCalled();

    // Should exit edit mode
    expect(screen.queryByText('Save')).not.toBeInTheDocument();
    expect(screen.queryByText('Cancel')).not.toBeInTheDocument();
  });

  it('navigates to user photos when View Images clicked', () => {
    renderUserTable();

    const viewButtons = screen.getAllByText('View Images');
    fireEvent.click(viewButtons[0]);

    expect(mockNavigate).toHaveBeenCalledWith('/photos/1');
  });

  it('only edits one user at a time', () => {
    renderUserTable();

    const editButtons = screen.getAllByText('Edit');

    // Click edit on first user
    fireEvent.click(editButtons[0]);
    expect(screen.getByText('Save')).toBeInTheDocument();

    // Cancel and edit second user
    fireEvent.click(screen.getByText('Cancel'));
    fireEvent.click(editButtons[1]);

    // Should only have one Save button
    expect(screen.getAllByText('Save').length).toBe(1);
  });

  it('handles empty user list', () => {
    render(
      <BrowserRouter>
        <UserTable
          users={[]}
          onSortChange={mockOnSort}
          onUserTypeChange={mockOnUserTypeChange}
          currentSort={mockCurrentSort}
        />
      </BrowserRouter>
    );

    expect(screen.getByText('No users found')).toBeInTheDocument();
  });

  it('calls onSortChange when column header clicked', () => {
    renderUserTable();

    const emailHeader = screen.getByText(/Email/); // Includes sort icon
    fireEvent.click(emailHeader);

    expect(mockOnSort).toHaveBeenCalledWith('email');
  });

  it('displays View Images button for all users', () => {
    renderUserTable();

    const viewButtons = screen.getAllByText('View Images');
    expect(viewButtons.length).toBe(mockUsers.length);
  });

  it('preserves user ID correctly in edit operations', () => {
    renderUserTable();

    // Edit the second user (Admin User, userId: 2)
    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[1]);

    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'USER' } });

    const saveButton = screen.getByText('Save');
    fireEvent.click(saveButton);

    expect(mockOnUserTypeChange).toHaveBeenCalledWith(2, 'USER');
  });

  it('formats null dates as "N/A"', () => {
    const usersWithNullDate = [
      { ...mockUsers[0], lastLoginDate: null }
    ];

    render(
      <BrowserRouter>
        <UserTable
          users={usersWithNullDate}
          onSortChange={mockOnSort}
          onUserTypeChange={mockOnUserTypeChange}
          currentSort={mockCurrentSort}
        />
      </BrowserRouter>
    );

    // Should find at least one N/A (could be from display name or date)
    expect(screen.getAllByText('N/A').length).toBeGreaterThan(0);
  });
});
