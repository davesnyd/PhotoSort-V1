/**
 * Mock Data for Tests
 * Copyright 2025, David Snyderman
 *
 * Reusable mock data for component testing
 */

export const mockUsers = [
  {
    userId: 1,
    email: 'user1@example.com',
    displayName: 'User One',
    userType: 'USER',
    photoCount: 5,
    firstLoginDate: '2024-01-15T10:30:00',
    lastLoginDate: '2024-12-01T14:20:00'
  },
  {
    userId: 2,
    email: 'admin@example.com',
    displayName: 'Admin User',
    userType: 'ADMIN',
    photoCount: 10,
    firstLoginDate: '2024-01-10T09:00:00',
    lastLoginDate: '2024-12-02T08:15:00'
  }
];

export const mockPhotos = [
  {
    photoId: 1,
    fileName: 'vacation.jpg',
    filePath: '/photos/vacation.jpg',
    fileSize: 2048576,
    imageWidth: 1920,
    imageHeight: 1080,
    fileCreatedDate: '2024-06-15T12:00:00',
    ownerDisplayName: 'User One',
    ownerId: 1,
    isPublic: true,
    thumbnailPath: '/thumbnails/vacation.jpg'
  },
  {
    photoId: 2,
    fileName: 'private.jpg',
    filePath: '/photos/private.jpg',
    fileSize: 1048576,
    imageWidth: 1280,
    imageHeight: 720,
    fileCreatedDate: '2024-07-20T15:30:00',
    ownerDisplayName: 'Admin User',
    ownerId: 2,
    isPublic: false,
    thumbnailPath: '/thumbnails/private.jpg'
  }
];

export const mockScripts = [
  {
    scriptId: 1,
    scriptName: 'Photo Resize Script',
    scriptFileName: 'resize_photos.py',
    scriptContents: '#!/usr/bin/env python3\nprint("Resizing photos")',
    runTime: '02:00:00',
    periodicityMinutes: null,
    fileExtension: '.jpg'
  },
  {
    scriptId: 2,
    scriptName: 'Metadata Extractor',
    scriptFileName: 'extract_metadata.sh',
    scriptContents: '#!/bin/bash\necho "Extracting metadata"',
    runTime: null,
    periodicityMinutes: 60,
    fileExtension: '.png'
  }
];

export const mockPagedResponse = (data) => ({
  content: data,
  page: 0,
  pageSize: 10,
  totalPages: Math.ceil(data.length / 10),
  totalElements: data.length
});

export const mockApiResponse = (data) => ({
  success: true,
  data: data,
  error: null
});

export const mockApiError = (message) => ({
  success: false,
  data: null,
  error: {
    code: 'ERROR',
    message: message
  }
});
