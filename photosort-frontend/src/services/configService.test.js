/**
 * Configuration Service Tests
 * Copyright 2025, David Snyderman
 */

import configService from './configService';
import api from './api';

// Mock the api module
jest.mock('./api');

describe('configService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getConfiguration', () => {
    it('fetches configuration with GET /api/config', async () => {
      const mockConfig = {
        success: true,
        data: {
          database: {
            uri: 'jdbc:postgresql://localhost:5432/PhotoSortData',
            username: 'postgres',
            password: '********'
          },
          git: {
            repoPath: '/path/to/repo',
            url: 'https://github.com/user/repo.git',
            username: 'gituser',
            token: '********',
            pollIntervalMinutes: 5
          },
          oauth: {
            clientId: 'test-client-id',
            clientSecret: '********',
            redirectUri: 'http://localhost:8080/oauth2/callback'
          },
          stag: {
            scriptPath: './stag-main/stag.py',
            pythonExecutable: 'python3'
          }
        }
      };

      api.get.mockResolvedValue({ data: mockConfig });

      const result = await configService.getConfiguration();

      expect(api.get).toHaveBeenCalledWith('/api/config');
      expect(result).toEqual(mockConfig);
      expect(result.data.database.password).toBe('********');
      expect(result.data.git.token).toBe('********');
      expect(result.data.oauth.clientSecret).toBe('********');
    });

    it('handles errors when fetching configuration', async () => {
      const mockError = new Error('Network error');
      api.get.mockRejectedValue(mockError);

      await expect(configService.getConfiguration()).rejects.toThrow('Network error');
    });
  });

  describe('updateConfiguration', () => {
    it('updates configuration with PUT /api/config', async () => {
      const updateData = {
        database: {
          uri: 'jdbc:postgresql://newhost:5432/PhotoSortData',
          username: 'postgres',
          password: '********'
        },
        git: {
          repoPath: '/new/repo/path',
          url: 'https://github.com/user/repo.git',
          username: 'gituser',
          token: '********',
          pollIntervalMinutes: 10
        },
        oauth: {
          clientId: 'test-client-id',
          clientSecret: '********',
          redirectUri: 'http://localhost:8080/oauth2/callback'
        },
        stag: {
          scriptPath: './stag-main/stag.py',
          pythonExecutable: 'python3'
        }
      };

      const mockResponse = {
        success: true,
        data: updateData
      };

      api.put.mockResolvedValue({ data: mockResponse });

      const result = await configService.updateConfiguration(updateData);

      expect(api.put).toHaveBeenCalledWith('/api/config', updateData);
      expect(result).toEqual(mockResponse);
    });

    it('handles errors when updating configuration', async () => {
      const updateData = {
        database: { uri: 'invalid-uri' }
      };

      const mockError = new Error('Invalid configuration');
      api.put.mockRejectedValue(mockError);

      await expect(configService.updateConfiguration(updateData)).rejects.toThrow('Invalid configuration');
    });

    it('sends new password values when changed', async () => {
      const updateData = {
        database: {
          uri: 'jdbc:postgresql://localhost:5432/PhotoSortData',
          username: 'postgres',
          password: 'newPassword123' // Changed password
        },
        git: {
          repoPath: '/path/to/repo',
          url: 'https://github.com/user/repo.git',
          username: 'gituser',
          token: 'newToken456', // Changed token
          pollIntervalMinutes: 5
        },
        oauth: {
          clientId: 'test-client-id',
          clientSecret: 'newSecret789', // Changed secret
          redirectUri: 'http://localhost:8080/oauth2/callback'
        },
        stag: {
          scriptPath: './stag-main/stag.py',
          pythonExecutable: 'python3'
        }
      };

      const mockResponse = {
        success: true,
        data: {
          ...updateData,
          database: { ...updateData.database, password: '********' },
          git: { ...updateData.git, token: '********' },
          oauth: { ...updateData.oauth, clientSecret: '********' }
        }
      };

      api.put.mockResolvedValue({ data: mockResponse });

      const result = await configService.updateConfiguration(updateData);

      expect(api.put).toHaveBeenCalledWith('/api/config', updateData);
      expect(result.data.database.password).toBe('********');
      expect(result.data.git.token).toBe('********');
      expect(result.data.oauth.clientSecret).toBe('********');
    });
  });
});
