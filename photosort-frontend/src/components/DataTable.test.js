/**
 * DataTable Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, fireEvent } from '@testing-library/react';
import DataTable from './DataTable';

describe('DataTable Component', () => {
  const mockColumns = [
    { field: 'id', header: 'ID', sortable: true },
    { field: 'name', header: 'Name', sortable: true },
    { field: 'status', header: 'Status', sortable: false }
  ];

  const mockData = [
    { id: 1, name: 'Test 1', status: 'active' },
    { id: 2, name: 'Test 2', status: 'inactive' }
  ];

  const mockOnSort = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders with empty data', () => {
    render(
      <DataTable
        data={[]}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
      />
    );

    expect(screen.getByText('No data found')).toBeInTheDocument();
  });

  it('renders rows with provided data', () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
      />
    );

    expect(screen.getByText('Test 1')).toBeInTheDocument();
    expect(screen.getByText('Test 2')).toBeInTheDocument();
    expect(screen.getByText('active')).toBeInTheDocument();
    expect(screen.getByText('inactive')).toBeInTheDocument();
  });

  it('renders custom column renderers', () => {
    const columnsWithRender = [
      { field: 'id', header: 'ID', sortable: true },
      {
        field: 'name',
        header: 'Name',
        sortable: true,
        render: (row, value) => <span data-testid="custom-render">{value.toUpperCase()}</span>
      }
    ];

    render(
      <DataTable
        data={mockData}
        columns={columnsWithRender}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
      />
    );

    expect(screen.getByText('TEST 1')).toBeInTheDocument();
    expect(screen.getByText('TEST 2')).toBeInTheDocument();
  });

  it('calls onSort when sortable column clicked', () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
      />
    );

    const nameHeader = screen.getByText((content, element) =>
      element.tagName === 'TH' && content.includes('Name')
    );
    fireEvent.click(nameHeader);

    expect(mockOnSort).toHaveBeenCalledWith('name');
  });

  it('does not call onSort for non-sortable columns', () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
      />
    );

    const statusHeader = screen.getByText('Status');
    fireEvent.click(statusHeader);

    expect(mockOnSort).not.toHaveBeenCalled();
  });

  it('shows sort indicator on current sort column', () => {
    const { rerender } = render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
      />
    );

    const idHeader = screen.getByText((content, element) =>
      element.tagName === 'TH' && content.includes('ID')
    );
    expect(idHeader.textContent).toContain('↑');

    rerender(
      <DataTable
        data={mockData}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'desc' }}
        keyField="id"
      />
    );

    const idHeaderDesc = screen.getByText((content, element) =>
      element.tagName === 'TH' && content.includes('ID')
    );
    expect(idHeaderDesc.textContent).toContain('↓');
  });

  it('renders action buttons via renderActions prop', () => {
    const renderActions = (row) => (
      <button data-testid={`action-${row.id}`}>Edit</button>
    );

    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        renderActions={renderActions}
        keyField="id"
      />
    );

    expect(screen.getByTestId('action-1')).toBeInTheDocument();
    expect(screen.getByTestId('action-2')).toBeInTheDocument();
  });

  it('uses correct keyField for row keys', () => {
    const { container } = render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
      />
    );

    const rows = container.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(2);
  });

  it('shows custom "no data" message', () => {
    render(
      <DataTable
        data={[]}
        columns={mockColumns}
        onSort={mockOnSort}
        currentSort={{ field: 'id', direction: 'asc' }}
        keyField="id"
        noDataMessage="Nothing to show"
      />
    );

    expect(screen.getByText('Nothing to show')).toBeInTheDocument();
  });
});
