/*
 * Copyright 2025, David Snyderman
 *
 * CustomMetadataSection Component Tests
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import CustomMetadataSection from './CustomMetadataSection';

describe('CustomMetadataSection Component', () => {
  const mockOnUpdate = jest.fn();

  const mockMetadata = [
    { metadataId: 1, fieldName: 'location', metadataValue: 'San Francisco' },
    { metadataId: 2, fieldName: 'event', metadataValue: 'Wedding 2024' }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders all metadata fields', () => {
    render(<CustomMetadataSection metadata={mockMetadata} onUpdate={mockOnUpdate} />);

    expect(screen.getByText(/location/i)).toBeInTheDocument();
    expect(screen.getByText('San Francisco')).toBeInTheDocument();
    expect(screen.getByText(/event/i)).toBeInTheDocument();
    expect(screen.getByText('Wedding 2024')).toBeInTheDocument();
  });

  test('displays no metadata message when empty', () => {
    render(<CustomMetadataSection metadata={[]} onUpdate={mockOnUpdate} />);

    expect(screen.getByText(/No custom metadata fields/i)).toBeInTheDocument();
  });

  test('shows Add Field button', () => {
    render(<CustomMetadataSection metadata={[]} onUpdate={mockOnUpdate} />);

    expect(screen.getByText(/Add Field/i)).toBeInTheDocument();
  });

  test('opens modal when Add Field button clicked', () => {
    render(<CustomMetadataSection metadata={[]} onUpdate={mockOnUpdate} />);

    const addButton = screen.getByText(/Add Field/i);
    fireEvent.click(addButton);

    expect(screen.getByText(/Add Metadata Field/i)).toBeInTheDocument();
  });
});
