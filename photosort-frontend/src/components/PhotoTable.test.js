/**
 * PhotoTable Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, fireEvent } from '@testing-library/react';
import PhotoTable from './PhotoTable';
import { mockPhotos } from '../test-utils/mockData';

describe('PhotoTable Component', () => {
  const mockOnSort = jest.fn();
  const mockCurrentSort = { field: 'fileName', direction: 'asc' };

  beforeEach(() => {
    jest.clearAllMocks();
    // Mock window.open
    global.open = jest.fn();
  });

  it('renders photo data in table', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    expect(screen.getByText('vacation.jpg')).toBeInTheDocument();
    expect(screen.getByText('private.jpg')).toBeInTheDocument();
  });

  it('displays thumbnails correctly', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    const thumbnail = screen.getAllByRole('img')[0];
    expect(thumbnail).toHaveAttribute('src', '/thumbnails/vacation.jpg');
    expect(thumbnail).toHaveAttribute('alt', 'vacation.jpg');
  });

  it('shows "No Image" placeholder for missing thumbnails', () => {
    const photosWithoutThumbnail = [
      { ...mockPhotos[0], thumbnailPath: null }
    ];

    render(
      <PhotoTable
        photos={photosWithoutThumbnail}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    expect(screen.getByText('No Image')).toBeInTheDocument();
  });

  it('formats file sizes correctly', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    // 2048576 bytes = 2.0 MB
    expect(screen.getByText('2.0 MB')).toBeInTheDocument();
    // 1048576 bytes = 1.0 MB
    expect(screen.getByText('1.0 MB')).toBeInTheDocument();
  });

  it('formats dimensions correctly', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    expect(screen.getByText('1920 × 1080')).toBeInTheDocument();
    expect(screen.getByText('1280 × 720')).toBeInTheDocument();
  });

  it('shows public/private badge with correct styling', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    const publicBadge = screen.getByText('Public');
    expect(publicBadge).toHaveClass('visibility-badge', 'public');

    const privateBadge = screen.getByText('Private');
    expect(privateBadge).toHaveClass('visibility-badge', 'private');
  });

  it('opens full image in new tab when View clicked', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    const viewButtons = screen.getAllByText('View');
    fireEvent.click(viewButtons[0]);

    expect(global.open).toHaveBeenCalledWith('/photos/vacation.jpg', '_blank');
  });

  it('formats dates correctly', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    // Dates should be formatted (exact format depends on locale)
    expect(screen.getByText(/Jun.*2024/)).toBeInTheDocument();
    expect(screen.getByText(/Jul.*2024/)).toBeInTheDocument();
  });

  it('displays owner names', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    expect(screen.getByText('User One')).toBeInTheDocument();
    expect(screen.getByText('Admin User')).toBeInTheDocument();
  });

  it('handles empty photo list', () => {
    render(
      <PhotoTable
        photos={[]}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    expect(screen.getByText('No photos found')).toBeInTheDocument();
  });

  it('calls onSortChange when column header clicked', () => {
    render(
      <PhotoTable
        photos={mockPhotos}
        onSortChange={mockOnSort}
        currentSort={mockCurrentSort}
      />
    );

    const sizeHeader = screen.getByText((content, element) =>
      element.tagName === 'TH' && content.includes('Size')
    );
    fireEvent.click(sizeHeader);

    expect(mockOnSort).toHaveBeenCalledWith('fileSize');
  });
});
