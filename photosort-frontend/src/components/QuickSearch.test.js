/**
 * QuickSearch Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, fireEvent } from '@testing-library/react';
import QuickSearch from './QuickSearch';

describe('QuickSearch Component', () => {
  const mockOnSearch = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders search input and button', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    expect(screen.getByPlaceholderText('Search by email or name...')).toBeInTheDocument();
    expect(screen.getByText('Search')).toBeInTheDocument();
  });

  it('updates input value when typing', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');
    fireEvent.change(input, { target: { value: 'test query' } });

    expect(input).toHaveValue('test query');
  });

  it('calls onSearch with trimmed value when form submitted', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');
    fireEvent.change(input, { target: { value: '  test query  ' } });

    const form = input.closest('form');
    fireEvent.submit(form);

    expect(mockOnSearch).toHaveBeenCalledWith('test query');
  });

  it('calls onSearch when Search button clicked', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');
    fireEvent.change(input, { target: { value: 'test' } });

    const searchButton = screen.getByText('Search');
    fireEvent.click(searchButton);

    expect(mockOnSearch).toHaveBeenCalledWith('test');
  });

  it('shows Clear button when input has text', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');

    // No Clear button initially
    expect(screen.queryByText('Clear')).not.toBeInTheDocument();

    // Clear button appears when typing
    fireEvent.change(input, { target: { value: 'test' } });
    expect(screen.getByText('Clear')).toBeInTheDocument();
  });

  it('hides Clear button when input is empty', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');

    // Add text
    fireEvent.change(input, { target: { value: 'test' } });
    expect(screen.getByText('Clear')).toBeInTheDocument();

    // Remove text
    fireEvent.change(input, { target: { value: '' } });
    expect(screen.queryByText('Clear')).not.toBeInTheDocument();
  });

  it('clears input and calls onSearch with empty string when Clear clicked', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');
    fireEvent.change(input, { target: { value: 'test' } });

    const clearButton = screen.getByText('Clear');
    fireEvent.click(clearButton);

    expect(input).toHaveValue('');
    expect(mockOnSearch).toHaveBeenCalledWith('');
  });

  it('trims whitespace from search term', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');
    fireEvent.change(input, { target: { value: '   spaces   ' } });

    const searchButton = screen.getByText('Search');
    fireEvent.click(searchButton);

    expect(mockOnSearch).toHaveBeenCalledWith('spaces');
  });

  it('allows searching with empty string', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');
    const searchButton = screen.getByText('Search');

    fireEvent.click(searchButton);

    expect(mockOnSearch).toHaveBeenCalledWith('');
  });

  it('prevents default form submission', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const form = screen.getByPlaceholderText('Search by email or name...').closest('form');
    const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
    const preventDefaultSpy = jest.spyOn(submitEvent, 'preventDefault');

    form.dispatchEvent(submitEvent);

    expect(preventDefaultSpy).toHaveBeenCalled();
  });

  it('Clear button has correct type to prevent form submission', () => {
    render(<QuickSearch onSearch={mockOnSearch} />);

    const input = screen.getByPlaceholderText('Search by email or name...');
    fireEvent.change(input, { target: { value: 'test' } });

    const clearButton = screen.getByText('Clear');
    expect(clearButton).toHaveAttribute('type', 'button');
  });
});
