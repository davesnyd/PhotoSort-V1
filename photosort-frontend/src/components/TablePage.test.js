/**
 * TablePage Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen } from '@testing-library/react';
import TablePage from './TablePage';

describe('TablePage Component', () => {
  it('renders title and subtitle', () => {
    render(
      <TablePage title="Test Title" subtitle="Test Subtitle">
        <div>Content</div>
      </TablePage>
    );

    expect(screen.getByText('Test Title')).toBeInTheDocument();
    expect(screen.getByText('Test Subtitle')).toBeInTheDocument();
  });

  it('renders children content', () => {
    render(
      <TablePage title="Test" subtitle="Subtitle">
        <div data-testid="child-content">Child Content</div>
      </TablePage>
    );

    expect(screen.getByTestId('child-content')).toBeInTheDocument();
    expect(screen.getByText('Child Content')).toBeInTheDocument();
  });

  it('applies correct CSS classes', () => {
    const { container } = render(
      <TablePage title="Test" subtitle="Subtitle">
        <div>Content</div>
      </TablePage>
    );

    expect(container.querySelector('.table-page')).toBeInTheDocument();
  });

  it('renders without subtitle', () => {
    render(
      <TablePage title="Test Title">
        <div>Content</div>
      </TablePage>
    );

    expect(screen.getByText('Test Title')).toBeInTheDocument();
    expect(screen.queryByText('Subtitle')).not.toBeInTheDocument();
  });
});
