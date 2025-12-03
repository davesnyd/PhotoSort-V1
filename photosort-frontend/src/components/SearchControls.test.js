/**
 * SearchControls Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, fireEvent } from '@testing-library/react';
import SearchControls from './SearchControls';

// Mock child components
jest.mock('./QuickSearch', () => {
  return function QuickSearch({ onSearch }) {
    return (
      <div data-testid="quick-search">
        <button onClick={() => onSearch('test query')}>Quick Search Mock</button>
      </div>
    );
  };
});

jest.mock('./AdvancedSearch', () => {
  return function AdvancedSearch({ onSearch }) {
    return (
      <div data-testid="advanced-search">
        <button onClick={() => onSearch([{ field: 'test' }])}>Advanced Search Mock</button>
      </div>
    );
  };
});

describe('SearchControls Component', () => {
  const mockOnQuickSearch = jest.fn();
  const mockOnAdvancedSearch = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders with Quick Search tab active by default', () => {
    render(
      <SearchControls
        onQuickSearch={mockOnQuickSearch}
        onAdvancedSearch={mockOnAdvancedSearch}
      />
    );

    const quickTab = screen.getByText('Quick Search');
    expect(quickTab).toHaveClass('active');
    expect(screen.getByTestId('quick-search')).toBeInTheDocument();
  });

  it('switches to Advanced Search tab when clicked', () => {
    render(
      <SearchControls
        onQuickSearch={mockOnQuickSearch}
        onAdvancedSearch={mockOnAdvancedSearch}
      />
    );

    const advancedTab = screen.getByText('Advanced Search');
    fireEvent.click(advancedTab);

    expect(advancedTab).toHaveClass('active');
    expect(screen.getByTestId('advanced-search')).toBeInTheDocument();
  });

  it('switches back to Quick Search tab', () => {
    render(
      <SearchControls
        onQuickSearch={mockOnQuickSearch}
        onAdvancedSearch={mockOnAdvancedSearch}
      />
    );

    const advancedTab = screen.getByText('Advanced Search');
    const quickTab = screen.getByText('Quick Search');

    // Switch to advanced
    fireEvent.click(advancedTab);
    expect(screen.getByTestId('advanced-search')).toBeInTheDocument();

    // Switch back to quick
    fireEvent.click(quickTab);
    expect(screen.getByTestId('quick-search')).toBeInTheDocument();
  });

  it('forwards quick search to callback', () => {
    render(
      <SearchControls
        onQuickSearch={mockOnQuickSearch}
        onAdvancedSearch={mockOnAdvancedSearch}
      />
    );

    const mockButton = screen.getByText('Quick Search Mock');
    fireEvent.click(mockButton);

    expect(mockOnQuickSearch).toHaveBeenCalledWith('test query');
  });

  it('forwards advanced search to callback', () => {
    render(
      <SearchControls
        onQuickSearch={mockOnQuickSearch}
        onAdvancedSearch={mockOnAdvancedSearch}
      />
    );

    const advancedTab = screen.getByText('Advanced Search');
    fireEvent.click(advancedTab);

    const mockButton = screen.getByText('Advanced Search Mock');
    fireEvent.click(mockButton);

    expect(mockOnAdvancedSearch).toHaveBeenCalledWith([{ field: 'test' }]);
  });

  it('only shows one search component at a time', () => {
    render(
      <SearchControls
        onQuickSearch={mockOnQuickSearch}
        onAdvancedSearch={mockOnAdvancedSearch}
      />
    );

    // Initially shows quick search
    expect(screen.getByTestId('quick-search')).toBeInTheDocument();
    expect(screen.queryByTestId('advanced-search')).not.toBeInTheDocument();

    // After switching shows advanced search
    fireEvent.click(screen.getByText('Advanced Search'));
    expect(screen.queryByTestId('quick-search')).not.toBeInTheDocument();
    expect(screen.getByTestId('advanced-search')).toBeInTheDocument();
  });

  it('applies correct CSS class to active tab', () => {
    render(
      <SearchControls
        onQuickSearch={mockOnQuickSearch}
        onAdvancedSearch={mockOnAdvancedSearch}
      />
    );

    const quickTab = screen.getByText('Quick Search');
    const advancedTab = screen.getByText('Advanced Search');

    // Quick tab is active by default
    expect(quickTab).toHaveClass('search-tab', 'active');
    expect(advancedTab).toHaveClass('search-tab');
    expect(advancedTab).not.toHaveClass('active');

    // After switching, advanced tab is active
    fireEvent.click(advancedTab);
    expect(advancedTab).toHaveClass('search-tab', 'active');
    expect(quickTab).toHaveClass('search-tab');
    expect(quickTab).not.toHaveClass('active');
  });
});
