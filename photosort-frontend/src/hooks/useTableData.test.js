/**
 * useTableData Hook Tests
 * Copyright 2025, David Snyderman
 */

import { renderHook, act, waitFor } from '@testing-library/react';
import useTableData from './useTableData';
import { mockApiResponse, mockPagedResponse, mockUsers } from '../test-utils/mockData';

describe('useTableData Hook', () => {
  let mockFetchFunction;

  beforeEach(() => {
    mockFetchFunction = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('fetches data on mount', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    expect(result.current.loading).toBe(true);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockFetchFunction).toHaveBeenCalledTimes(1);
    expect(result.current.data).toEqual(mockUsers);
    expect(result.current.error).toBeNull();
  });

  it('shows loading state during fetch', () => {
    mockFetchFunction.mockImplementation(() => new Promise(() => {})); // Never resolves

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    expect(result.current.loading).toBe(true);
    expect(result.current.data).toEqual([]);
  });

  it('handles fetch errors', async () => {
    const errorMessage = 'Network error';
    mockFetchFunction.mockRejectedValue(new Error(errorMessage));

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('Error loading data');
    expect(result.current.data).toEqual([]);
  });

  it('handles page change', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    act(() => {
      result.current.handlePageChange(2);
    });

    await waitFor(() => {
      expect(mockFetchFunction).toHaveBeenCalledWith(
        expect.objectContaining({ page: 2 })
      );
    });

    expect(result.current.currentPage).toBe(2);
  });

  it('handles sort change', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    act(() => {
      result.current.handleSortChange('name');
    });

    await waitFor(() => {
      expect(result.current.sortBy).toBe('name');
    });
  });

  it('toggles sort direction when clicking same column', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.sortDir).toBe('asc');

    act(() => {
      result.current.handleSortChange('id'); // Same field
    });

    await waitFor(() => {
      expect(result.current.sortDir).toBe('desc');
    });
  });

  it('handles quick search', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    act(() => {
      result.current.handleQuickSearch('test');
    });

    await waitFor(() => {
      expect(mockFetchFunction).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'test', page: 0 })
      );
    });
  });

  it('resets to page 0 when search changes', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    // Go to page 2
    act(() => {
      result.current.handlePageChange(2);
    });

    await waitFor(() => {
      expect(result.current.currentPage).toBe(2);
    });

    // Search should reset to page 0
    act(() => {
      result.current.handleQuickSearch('test');
    });

    await waitFor(() => {
      expect(result.current.currentPage).toBe(0);
    });
  });

  it('refresh function re-fetches data', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    const initialCallCount = mockFetchFunction.mock.calls.length;

    act(() => {
      result.current.refresh();
    });

    await waitFor(() => {
      expect(mockFetchFunction.mock.calls.length).toBe(initialCallCount + 1);
    });
  });

  it('handles advanced search with filters', async () => {
    const responseData = mockApiResponse(mockPagedResponse(mockUsers));
    mockFetchFunction.mockResolvedValue(responseData);

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    const filters = [
      { field: 'name', value: 'test', type: 'MUST_CONTAIN' }
    ];

    act(() => {
      result.current.handleAdvancedSearch(filters);
    });

    await waitFor(() => {
      expect(mockFetchFunction).toHaveBeenCalledWith(
        expect.objectContaining({ filters, page: 0 })
      );
    });
  });
});
