/**
 * photoService Tests
 * Copyright 2025, David Snyderman
 */

import photoService from './photoService';
import api from './api';
import { mockPhotos, mockApiResponse, mockPagedResponse } from '../test-utils/mockData';

// Mock the api module
jest.mock('./api');

describe('photoService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getPhotos', () => {
    it('calls API with default parameters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos();

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/photos?')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('page=0')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('size=10')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('sort=fileName')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('direction=asc')
      );
    });

    it('calls API with custom pagination parameters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({ page: 2, size: 20 });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('page=2')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('size=20')
      );
    });

    it('calls API with custom sort parameters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({ sort: 'fileSize', direction: 'desc' });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('sort=fileSize')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('direction=desc')
      );
    });

    it('includes search parameter when provided', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({ search: 'vacation' });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('search=vacation')
      );
    });

    it('trims whitespace from search parameter', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({ search: '  vacation  ' });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('search=vacation')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.not.stringContaining('search=%20%20vacation')
      );
    });

    it('excludes search parameter when empty', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({ search: '' });

      const callUrl = api.get.mock.calls[0][0];
      expect(callUrl).not.toContain('search=');
    });

    it('includes userId parameter when provided', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({ userId: 123 });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('userId=123')
      );
    });

    it('includes advanced filter parameters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({
        filterField1: 'fileName',
        filterValue1: 'test',
        filterType1: 'MUST_CONTAIN'
      });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('filterField1=fileName')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('filterValue1=test')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('filterType1=MUST_CONTAIN')
      );
    });

    it('includes multiple advanced filters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({
        filterField1: 'fileName',
        filterValue1: 'test',
        filterType1: 'MUST_CONTAIN',
        filterField2: 'ownerDisplayName',
        filterValue2: 'admin',
        filterType2: 'MUST_NOT_CONTAIN'
      });

      const callUrl = api.get.mock.calls[0][0];
      expect(callUrl).toContain('filterField1=fileName');
      expect(callUrl).toContain('filterField2=ownerDisplayName');
      expect(callUrl).toContain('filterValue1=test');
      expect(callUrl).toContain('filterValue2=admin');
    });

    it('does not include incomplete filter 1', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getPhotos({
        filterField1: 'fileName',
        filterValue1: 'test'
        // Missing filterType1
      });

      const callUrl = api.get.mock.calls[0][0];
      expect(callUrl).not.toContain('filterField1');
    });

    it('returns data from API response', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockPhotos));
      api.get.mockResolvedValue({ data: responseData });

      const result = await photoService.getPhotos();

      expect(result).toEqual(responseData);
    });

    it('propagates errors from API', async () => {
      const error = new Error('Network error');
      api.get.mockRejectedValue(error);

      await expect(photoService.getPhotos()).rejects.toThrow('Network error');
    });
  });

  describe('getUserColumns', () => {
    it('calls API with correct endpoint', async () => {
      const responseData = mockApiResponse([]);
      api.get.mockResolvedValue({ data: responseData });

      await photoService.getUserColumns(123);

      expect(api.get).toHaveBeenCalledWith('/api/users/123/columns');
    });

    it('returns data from API response', async () => {
      const columns = [
        { columnName: 'fileName', visible: true, order: 1 }
      ];
      const responseData = mockApiResponse(columns);
      api.get.mockResolvedValue({ data: responseData });

      const result = await photoService.getUserColumns(123);

      expect(result).toEqual(responseData);
    });

    it('propagates errors from API', async () => {
      const error = new Error('Not found');
      api.get.mockRejectedValue(error);

      await expect(photoService.getUserColumns(123)).rejects.toThrow('Not found');
    });
  });

  describe('updateUserColumns', () => {
    it('calls API with correct endpoint and data', async () => {
      const columns = [
        { columnName: 'fileName', visible: true, order: 1 }
      ];
      const responseData = mockApiResponse({ success: true });
      api.put.mockResolvedValue({ data: responseData });

      await photoService.updateUserColumns(123, columns);

      expect(api.put).toHaveBeenCalledWith('/api/users/123/columns', columns);
    });

    it('returns data from API response', async () => {
      const columns = [
        { columnName: 'fileName', visible: true, order: 1 }
      ];
      const responseData = mockApiResponse({ success: true });
      api.put.mockResolvedValue({ data: responseData });

      const result = await photoService.updateUserColumns(123, columns);

      expect(result).toEqual(responseData);
    });

    it('propagates errors from API', async () => {
      const error = new Error('Update failed');
      api.put.mockRejectedValue(error);

      await expect(photoService.updateUserColumns(123, [])).rejects.toThrow('Update failed');
    });
  });
});
