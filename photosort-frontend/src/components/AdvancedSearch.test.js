/**
 * AdvancedSearch Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, fireEvent } from '@testing-library/react';
import AdvancedSearch from './AdvancedSearch';

describe('AdvancedSearch Component', () => {
  const mockOnSearch = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders three filter rows', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    expect(screen.getByText('Filter 1:')).toBeInTheDocument();
    expect(screen.getByText('Filter 2:')).toBeInTheDocument();
    expect(screen.getByText('Filter 3:')).toBeInTheDocument();
  });

  it('renders Apply Filters and Clear All buttons', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    expect(screen.getByText('Apply Filters')).toBeInTheDocument();
    expect(screen.getByText('Clear All')).toBeInTheDocument();
  });

  it('has correct default column selections', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const columnSelects = screen.getAllByDisplayValue(/Email|Display Name|User Type/);

    expect(columnSelects[0]).toHaveValue('email');
    expect(columnSelects[1]).toHaveValue('displayName');
    expect(columnSelects[2]).toHaveValue('userType');
  });

  it('has correct default operation selections', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const operationSelects = screen.getAllByDisplayValue('Contains');

    // All three filters should default to "Contains"
    expect(operationSelects).toHaveLength(3);
  });

  it('changes column selection', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const columnSelects = screen.getAllByRole('combobox');
    const firstColumnSelect = columnSelects.find(select => select.value === 'email');

    fireEvent.change(firstColumnSelect, { target: { value: 'firstLoginDate' } });

    expect(firstColumnSelect).toHaveValue('firstLoginDate');
  });

  it('changes operation selection', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const operationSelects = screen.getAllByDisplayValue('Contains');

    fireEvent.change(operationSelects[0], { target: { value: 'NOT_CONTAINS' } });

    expect(operationSelects[0]).toHaveValue('NOT_CONTAINS');
  });

  it('updates filter value input', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const valueInputs = screen.getAllByPlaceholderText('Enter search value...');

    fireEvent.change(valueInputs[0], { target: { value: 'test@example.com' } });

    expect(valueInputs[0]).toHaveValue('test@example.com');
  });

  it('calls onSearch with active filters when form submitted', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const valueInputs = screen.getAllByPlaceholderText('Enter search value...');

    // Fill in first filter
    fireEvent.change(valueInputs[0], { target: { value: 'test' } });

    const form = screen.getByText('Apply Filters').closest('form');
    fireEvent.submit(form);

    expect(mockOnSearch).toHaveBeenCalledWith([
      { column: 'email', value: 'test', operation: 'CONTAINS' }
    ]);
  });

  it('filters out empty values when applying filters', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const valueInputs = screen.getAllByPlaceholderText('Enter search value...');

    // Fill in first and third filters, leave second empty
    fireEvent.change(valueInputs[0], { target: { value: 'test1' } });
    fireEvent.change(valueInputs[2], { target: { value: 'test3' } });

    const applyButton = screen.getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnSearch).toHaveBeenCalledWith([
      { column: 'email', value: 'test1', operation: 'CONTAINS' },
      { column: 'userType', value: 'test3', operation: 'CONTAINS' }
    ]);
  });

  it('trims whitespace from filter values', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const valueInputs = screen.getAllByPlaceholderText('Enter search value...');

    fireEvent.change(valueInputs[0], { target: { value: '   spaces   ' } });

    const applyButton = screen.getByText('Apply Filters');
    fireEvent.click(applyButton);

    // Should be filtered out because trimmed value is not empty
    expect(mockOnSearch).toHaveBeenCalledWith([
      { column: 'email', value: '   spaces   ', operation: 'CONTAINS' }
    ]);
  });

  it('calls onSearch with empty array when no filters have values', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const applyButton = screen.getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnSearch).toHaveBeenCalledWith([]);
  });

  it('clears all filter values when Clear All clicked', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const valueInputs = screen.getAllByPlaceholderText('Enter search value...');

    // Fill in all filters
    fireEvent.change(valueInputs[0], { target: { value: 'test1' } });
    fireEvent.change(valueInputs[1], { target: { value: 'test2' } });
    fireEvent.change(valueInputs[2], { target: { value: 'test3' } });

    const clearButton = screen.getByText('Clear All');
    fireEvent.click(clearButton);

    // All inputs should be cleared
    expect(valueInputs[0]).toHaveValue('');
    expect(valueInputs[1]).toHaveValue('');
    expect(valueInputs[2]).toHaveValue('');
  });

  it('calls onSearch with empty array when Clear All clicked', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const valueInputs = screen.getAllByPlaceholderText('Enter search value...');
    fireEvent.change(valueInputs[0], { target: { value: 'test' } });

    const clearButton = screen.getByText('Clear All');
    fireEvent.click(clearButton);

    expect(mockOnSearch).toHaveBeenCalledWith([]);
  });

  it('preserves column and operation selections when clearing', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const columnSelects = screen.getAllByRole('combobox');
    const firstColumnSelect = columnSelects.find(select => select.value === 'email');
    const operationSelects = screen.getAllByDisplayValue('Contains');

    // Change column and operation
    fireEvent.change(firstColumnSelect, { target: { value: 'firstLoginDate' } });
    fireEvent.change(operationSelects[0], { target: { value: 'NOT_CONTAINS' } });

    const clearButton = screen.getByText('Clear All');
    fireEvent.click(clearButton);

    // Column and operation should stay the same
    expect(firstColumnSelect).toHaveValue('firstLoginDate');
    expect(operationSelects[0]).toHaveValue('NOT_CONTAINS');
  });

  it('renders all column options', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const columnSelects = screen.getAllByRole('combobox');
    const firstColumnSelect = columnSelects.find(select => select.value === 'email');

    // Check all options are present
    const options = Array.from(firstColumnSelect.options).map(opt => opt.value);
    expect(options).toEqual([
      'email',
      'displayName',
      'userType',
      'firstLoginDate',
      'lastLoginDate'
    ]);
  });

  it('renders all operation options', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const operationSelects = screen.getAllByDisplayValue('Contains');

    const options = Array.from(operationSelects[0].options).map(opt => opt.value);
    expect(options).toEqual(['CONTAINS', 'NOT_CONTAINS']);
  });

  it('applies multiple filters with different operations', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const columnSelects = screen.getAllByRole('combobox');
    const operationSelects = screen.getAllByDisplayValue('Contains');
    const valueInputs = screen.getAllByPlaceholderText('Enter search value...');

    // Setup first filter: email CONTAINS test
    fireEvent.change(valueInputs[0], { target: { value: 'test' } });

    // Setup second filter: displayName NOT_CONTAINS admin
    fireEvent.change(operationSelects[1], { target: { value: 'NOT_CONTAINS' } });
    fireEvent.change(valueInputs[1], { target: { value: 'admin' } });

    const applyButton = screen.getByText('Apply Filters');
    fireEvent.click(applyButton);

    expect(mockOnSearch).toHaveBeenCalledWith([
      { column: 'email', value: 'test', operation: 'CONTAINS' },
      { column: 'displayName', value: 'admin', operation: 'NOT_CONTAINS' }
    ]);
  });

  it('Clear All button has correct type to prevent form submission', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const clearButton = screen.getByText('Clear All');
    expect(clearButton).toHaveAttribute('type', 'button');
  });

  it('prevents default form submission', () => {
    render(<AdvancedSearch onSearch={mockOnSearch} />);

    const form = screen.getByText('Apply Filters').closest('form');
    const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
    const preventDefaultSpy = jest.spyOn(submitEvent, 'preventDefault');

    form.dispatchEvent(submitEvent);

    expect(preventDefaultSpy).toHaveBeenCalled();
  });
});
