/**
 * Photos Page Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Photos from './Photos';
import { mockPhotos, mockApiResponse, mockPagedResponse } from '../test-utils/mockData';
import photoService from '../services/photoService';

// Mock the photo service
jest.mock('../services/photoService');

describe('Photos Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderPhotosPage = (initialRoute = '/photos') => {
    window.history.pushState({}, 'Test page', initialRoute);
    return render(
      <BrowserRouter>
        <Routes>
          <Route path="/photos" element={<Photos />} />
          <Route path="/photos/:userId" element={<Photos />} />
        </Routes>
      </BrowserRouter>
    );
  };

  it('renders page title and subtitle for current user', async () => {
    photoService.getPhotos.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockPhotos))
    );

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('My Photos')).toBeInTheDocument();
    });

    expect(
      screen.getByText('View and manage your photos with permission-based access')
    ).toBeInTheDocument();
  });

  it('shows loading state initially', () => {
    photoService.getPhotos.mockImplementation(() => new Promise(() => {}));

    renderPhotosPage();

    expect(screen.getByText('Loading photos...')).toBeInTheDocument();
  });

  it('displays photos after loading', async () => {
    photoService.getPhotos.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockPhotos))
    );

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('vacation.jpg')).toBeInTheDocument();
    });

    expect(screen.getByText('private.jpg')).toBeInTheDocument();
  });

  it('displays error message on fetch failure', async () => {
    photoService.getPhotos.mockRejectedValue(new Error('Network error'));

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText(/Error loading data/)).toBeInTheDocument();
    });
  });

  it('renders SearchControls component', async () => {
    photoService.getPhotos.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockPhotos))
    );

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('Quick Search')).toBeInTheDocument();
    });

    expect(screen.getByText('Advanced Search')).toBeInTheDocument();
  });

  it('renders PhotoTable component with photos', async () => {
    photoService.getPhotos.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockPhotos))
    );

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('vacation.jpg')).toBeInTheDocument();
    });

    // Check for photo table column headers (with sort icons)
    expect(screen.getByText(/File Name/)).toBeInTheDocument();
    expect(screen.getByText(/Size/)).toBeInTheDocument();
  });

  it('displays results summary with correct count', async () => {
    photoService.getPhotos.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockPhotos))
    );

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('Showing 2 of 2 photos')).toBeInTheDocument();
    });
  });

  it('renders PaginationControls when multiple pages', async () => {
    const pagedData = {
      content: mockPhotos,
      page: 0,
      pageSize: 10,
      totalPages: 3,
      totalElements: 25
    };

    photoService.getPhotos.mockResolvedValue(mockApiResponse(pagedData));

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    });
  });

  it('does not render pagination with single page', async () => {
    photoService.getPhotos.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockPhotos))
    );

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('vacation.jpg')).toBeInTheDocument();
    });

    // Pagination should not be visible
    expect(screen.queryByText(/Page 1 of 1/)).not.toBeInTheDocument();
  });

  it('uses correct default sort (fileName, asc)', async () => {
    photoService.getPhotos.mockResolvedValue(
      mockApiResponse(mockPagedResponse(mockPhotos))
    );

    renderPhotosPage();

    await waitFor(() => {
      expect(photoService.getPhotos).toHaveBeenCalledWith(
        expect.objectContaining({
          sortBy: 'fileName',
          sortDir: 'asc'
        })
      );
    });
  });

  it('shows singular "photo" for count of 1', async () => {
    const singlePhoto = {
      content: [mockPhotos[0]],
      page: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 1
    };

    photoService.getPhotos.mockResolvedValue(mockApiResponse(singlePhoto));

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('Showing 1 of 1 photo')).toBeInTheDocument();
    });
  });

  it('does not show results summary while loading', () => {
    photoService.getPhotos.mockImplementation(() => new Promise(() => {}));

    renderPhotosPage();

    expect(screen.queryByText(/Showing/)).not.toBeInTheDocument();
  });

  it('does not show results summary on error', async () => {
    photoService.getPhotos.mockRejectedValue(new Error('Network error'));

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText(/Error loading data/)).toBeInTheDocument();
    });

    expect(screen.queryByText(/Showing/)).not.toBeInTheDocument();
  });

  it('handles empty photo list', async () => {
    const emptyData = {
      content: [],
      page: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0
    };

    photoService.getPhotos.mockResolvedValue(mockApiResponse(emptyData));

    renderPhotosPage();

    await waitFor(() => {
      expect(screen.getByText('No photos found')).toBeInTheDocument();
    });
  });
});
