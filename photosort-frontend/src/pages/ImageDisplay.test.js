/*
 * Copyright 2025, David Snyderman
 *
 * ImageDisplay Component Tests
 */

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import ImageDisplay from './ImageDisplay';
import photoService from '../services/photoService';

// Mock photoService
jest.mock('../services/photoService');

// Mock react-router-dom hooks
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: () => ({ photoId: '123' }),
  useNavigate: () => mockNavigate,
}));

describe('ImageDisplay Component', () => {
  const mockPhotoDetail = {
    success: true,
    data: {
      photoId: 123,
      fileName: 'test-photo.jpg',
      filePath: '/path/to/test-photo.jpg',
      fileSize: 1024000,
      imageWidth: 1920,
      imageHeight: 1080,
      ownerId: 1,
      ownerEmail: 'owner@test.com',
      ownerDisplayName: 'Photo Owner',
      exifData: {
        cameraMake: 'Canon',
        cameraModel: 'EOS 5D',
        dateTimeOriginal: '2024-01-01T12:00:00',
        gpsLatitude: 37.7749,
        gpsLongitude: -122.4194,
        exposureTime: '1/125',
        fNumber: 'f/5.6',
        isoSpeed: 400,
        focalLength: '50mm'
      },
      metadata: [
        { metadataId: 1, fieldName: 'location', metadataValue: 'San Francisco' }
      ],
      tags: [
        { tagId: 1, tagValue: 'landscape' },
        { tagId: 2, tagValue: 'nature' }
      ]
    }
  };

  beforeEach(() => {
    jest.clearAllMocks();
    photoService.getPhotoDetail.mockResolvedValue(mockPhotoDetail);
    photoService.getPhotoImageUrl.mockReturnValue('/api/photos/123/image');
  });

  test('renders loading state initially', () => {
    render(
      <BrowserRouter>
        <ImageDisplay />
      </BrowserRouter>
    );

    expect(screen.getByText(/Loading photo.../i)).toBeInTheDocument();
  });

  test('renders photo details after loading', async () => {
    render(
      <BrowserRouter>
        <ImageDisplay />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('test-photo.jpg')).toBeInTheDocument();
      expect(screen.getByText(/1920 × 1080 pixels/i)).toBeInTheDocument();
    });
  });

  test('renders EXIF data section', async () => {
    render(
      <BrowserRouter>
        <ImageDisplay />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/EXIF Data/i)).toBeInTheDocument();
      expect(screen.getByText('Canon')).toBeInTheDocument();
      expect(screen.getByText('EOS 5D')).toBeInTheDocument();
    });
  });

  test('renders custom metadata section', async () => {
    render(
      <BrowserRouter>
        <ImageDisplay />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('San Francisco')).toBeInTheDocument();
    });

    // Check that metadata section is present
    const headings = screen.getAllByRole('heading', { level: 3 });
    expect(headings.some(h => h.textContent.includes('Custom Metadata'))).toBe(true);
  });

  test('renders tags section', async () => {
    render(
      <BrowserRouter>
        <ImageDisplay />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Tags/i)).toBeInTheDocument();
      expect(screen.getByText('landscape')).toBeInTheDocument();
      expect(screen.getByText('nature')).toBeInTheDocument();
    });
  });

  test('displays error when photo fails to load', async () => {
    photoService.getPhotoDetail.mockResolvedValue({
      success: false,
      error: 'Photo not found'
    });

    render(
      <BrowserRouter>
        <ImageDisplay />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Failed to load photo details/i)).toBeInTheDocument();
    });
  });

  test('navigates back to list when Return button clicked', async () => {
    render(
      <BrowserRouter>
        <ImageDisplay />
      </BrowserRouter>
    );

    await waitFor(() => {
      const returnButton = screen.getByText(/Return to List/i);
      returnButton.click();
    });

    expect(mockNavigate).toHaveBeenCalledWith('/photos');
  });
});
