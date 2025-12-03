/*
 * Copyright 2025, David Snyderman
 *
 * TagsSection Component Tests
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import TagsSection from './TagsSection';

describe('TagsSection Component', () => {
  const mockOnUpdate = jest.fn();

  const mockTags = [
    { tagId: 1, tagValue: 'landscape' },
    { tagId: 2, tagValue: 'nature' }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders all tags', () => {
    render(<TagsSection tags={mockTags} onUpdate={mockOnUpdate} />);

    expect(screen.getByText('landscape')).toBeInTheDocument();
    expect(screen.getByText('nature')).toBeInTheDocument();
  });

  test('displays no tags message when empty', () => {
    render(<TagsSection tags={[]} onUpdate={mockOnUpdate} />);

    expect(screen.getByText(/No tags/i)).toBeInTheDocument();
  });

  test('shows add tag input', () => {
    render(<TagsSection tags={[]} onUpdate={mockOnUpdate} />);

    expect(screen.getByPlaceholderText(/Add tag.../i)).toBeInTheDocument();
    expect(screen.getByText(/Add/i)).toBeInTheDocument();
  });

  test('adds new tag when Add button clicked', () => {
    render(<TagsSection tags={mockTags} onUpdate={mockOnUpdate} />);

    const input = screen.getByPlaceholderText(/Add tag.../i);
    const addButton = screen.getByText(/Add/i);

    fireEvent.change(input, { target: { value: 'sunset' } });
    fireEvent.click(addButton);

    expect(mockOnUpdate).toHaveBeenCalledWith(['landscape', 'nature', 'sunset']);
  });

  test('adds new tag when Enter key pressed', () => {
    render(<TagsSection tags={mockTags} onUpdate={mockOnUpdate} />);

    const input = screen.getByPlaceholderText(/Add tag.../i);

    fireEvent.change(input, { target: { value: 'mountains' } });
    fireEvent.keyPress(input, { key: 'Enter', code: 'Enter', charCode: 13 });

    expect(mockOnUpdate).toHaveBeenCalledWith(['landscape', 'nature', 'mountains']);
  });

  test('does not add duplicate tags', () => {
    render(<TagsSection tags={mockTags} onUpdate={mockOnUpdate} />);

    const input = screen.getByPlaceholderText(/Add tag.../i);
    const addButton = screen.getByText(/Add/i);

    fireEvent.change(input, { target: { value: 'landscape' } });
    fireEvent.click(addButton);

    expect(mockOnUpdate).not.toHaveBeenCalled();
  });
});
