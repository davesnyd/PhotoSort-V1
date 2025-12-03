/**
 * userService Tests
 * Copyright 2025, David Snyderman
 */

import userService from './userService';
import api from './api';
import { mockUsers, mockApiResponse, mockPagedResponse } from '../test-utils/mockData';

// Mock the api module
jest.mock('./api');

describe('userService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getUsers', () => {
    it('calls API with default parameters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      await userService.getUsers();

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('/api/users?')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('page=0')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('pageSize=10')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('sortBy=email')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('sortDir=asc')
      );
    });

    it('calls API with custom pagination parameters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      await userService.getUsers({ page: 3, pageSize: 25 });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('page=3')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('pageSize=25')
      );
    });

    it('calls API with custom sort parameters', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      await userService.getUsers({ sortBy: 'displayName', sortDir: 'desc' });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('sortBy=displayName')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('sortDir=desc')
      );
    });

    it('includes search parameter when provided', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      await userService.getUsers({ search: 'john' });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('search=john')
      );
    });

    it('trims whitespace from search parameter', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      await userService.getUsers({ search: '  john  ' });

      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining('search=john')
      );
      expect(api.get).toHaveBeenCalledWith(
        expect.not.stringContaining('search=%20%20john')
      );
    });

    it('excludes search parameter when empty', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      await userService.getUsers({ search: '' });

      const callUrl = api.get.mock.calls[0][0];
      expect(callUrl).not.toContain('search=');
    });

    it('includes filters parameter when provided', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      const filters = [
        { column: 'email', value: 'test', operation: 'CONTAINS' }
      ];

      await userService.getUsers({ filters });

      const callUrl = api.get.mock.calls[0][0];
      expect(callUrl).toContain('filters=');
      expect(callUrl).toContain(encodeURIComponent(JSON.stringify(filters)));
    });

    it('excludes filters parameter when empty array', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      await userService.getUsers({ filters: [] });

      const callUrl = api.get.mock.calls[0][0];
      expect(callUrl).not.toContain('filters=');
    });

    it('handles multiple filters correctly', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      const filters = [
        { column: 'email', value: 'test', operation: 'CONTAINS' },
        { column: 'userType', value: 'ADMIN', operation: 'NOT_CONTAINS' }
      ];

      await userService.getUsers({ filters });

      const callUrl = api.get.mock.calls[0][0];
      const decodedUrl = decodeURIComponent(callUrl);
      expect(decodedUrl).toContain(JSON.stringify(filters));
    });

    it('combines all parameters correctly', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      const filters = [
        { column: 'email', value: 'test', operation: 'CONTAINS' }
      ];

      await userService.getUsers({
        page: 2,
        pageSize: 15,
        sortBy: 'displayName',
        sortDir: 'desc',
        search: 'admin',
        filters
      });

      const callUrl = api.get.mock.calls[0][0];
      expect(callUrl).toContain('page=2');
      expect(callUrl).toContain('pageSize=15');
      expect(callUrl).toContain('sortBy=displayName');
      expect(callUrl).toContain('sortDir=desc');
      expect(callUrl).toContain('search=admin');
      expect(callUrl).toContain('filters=');
    });

    it('returns data from API response', async () => {
      const responseData = mockApiResponse(mockPagedResponse(mockUsers));
      api.get.mockResolvedValue({ data: responseData });

      const result = await userService.getUsers();

      expect(result).toEqual(responseData);
    });

    it('propagates errors from API', async () => {
      const error = new Error('Network error');
      api.get.mockRejectedValue(error);

      await expect(userService.getUsers()).rejects.toThrow('Network error');
    });
  });

  describe('updateUserType', () => {
    it('calls API with correct endpoint and data', async () => {
      const responseData = mockApiResponse({ userId: 123, userType: 'ADMIN' });
      api.put.mockResolvedValue({ data: responseData });

      await userService.updateUserType(123, 'ADMIN');

      expect(api.put).toHaveBeenCalledWith('/api/users/123', {
        userType: 'ADMIN'
      });
    });

    it('converts userType to uppercase', async () => {
      const responseData = mockApiResponse({ userId: 123, userType: 'ADMIN' });
      api.put.mockResolvedValue({ data: responseData });

      await userService.updateUserType(123, 'admin');

      expect(api.put).toHaveBeenCalledWith('/api/users/123', {
        userType: 'ADMIN'
      });
    });

    it('handles USER type correctly', async () => {
      const responseData = mockApiResponse({ userId: 456, userType: 'USER' });
      api.put.mockResolvedValue({ data: responseData });

      await userService.updateUserType(456, 'user');

      expect(api.put).toHaveBeenCalledWith('/api/users/456', {
        userType: 'USER'
      });
    });

    it('returns data from API response', async () => {
      const responseData = mockApiResponse({ userId: 123, userType: 'ADMIN' });
      api.put.mockResolvedValue({ data: responseData });

      const result = await userService.updateUserType(123, 'ADMIN');

      expect(result).toEqual(responseData);
    });

    it('propagates errors from API', async () => {
      const error = new Error('Update failed');
      api.put.mockRejectedValue(error);

      await expect(userService.updateUserType(123, 'ADMIN')).rejects.toThrow('Update failed');
    });

    it('handles 403 forbidden errors', async () => {
      const error = {
        response: {
          status: 403,
          data: { error: { message: 'Permission denied' } }
        }
      };
      api.put.mockRejectedValue(error);

      await expect(userService.updateUserType(123, 'ADMIN')).rejects.toEqual(error);
    });

    it('handles validation errors', async () => {
      const error = {
        response: {
          status: 400,
          data: { error: { message: 'Invalid user type' } }
        }
      };
      api.put.mockRejectedValue(error);

      await expect(userService.updateUserType(123, 'INVALID')).rejects.toEqual(error);
    });
  });
});
