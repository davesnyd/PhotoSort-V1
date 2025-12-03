/**
 * Test Utilities
 * Copyright 2025, David Snyderman
 *
 * Shared testing utilities for React components
 */

import React from 'react';
import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';

/**
 * Wrapper for components that need routing and auth context
 */
export function renderWithProviders(ui, options = {}) {
  const Wrapper = ({ children }) => (
    <BrowserRouter>
      <AuthProvider>
        {children}
      </AuthProvider>
    </BrowserRouter>
  );

  return render(ui, { wrapper: Wrapper, ...options });
}

/**
 * Mock axios instance
 */
export const createMockAxios = () => {
  return {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    interceptors: {
      request: { use: jest.fn(), eject: jest.fn() },
      response: { use: jest.fn(), eject: jest.fn() }
    }
  };
};
