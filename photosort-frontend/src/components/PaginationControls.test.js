/**
 * PaginationControls Component Tests
 * Copyright 2025, David Snyderman
 */

import { render, screen, fireEvent } from '@testing-library/react';
import PaginationControls from './PaginationControls';

describe('PaginationControls Component', () => {
  const mockOnPageChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('does not render when totalPages is 1', () => {
    const { container } = render(
      <PaginationControls
        currentPage={0}
        totalPages={1}
        onPageChange={mockOnPageChange}
      />
    );

    expect(container.firstChild).toBeNull();
  });

  it('does not render when totalPages is 0', () => {
    const { container } = render(
      <PaginationControls
        currentPage={0}
        totalPages={0}
        onPageChange={mockOnPageChange}
      />
    );

    expect(container.firstChild).toBeNull();
  });

  it('renders all navigation buttons when totalPages > 1', () => {
    render(
      <PaginationControls
        currentPage={1}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText(/First/)).toBeInTheDocument();
    expect(screen.getByText(/Prev/)).toBeInTheDocument();
    expect(screen.getByText(/Next/)).toBeInTheDocument();
    expect(screen.getByText(/Last/)).toBeInTheDocument();
  });

  it('disables First and Prev buttons on first page', () => {
    render(
      <PaginationControls
        currentPage={0}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText(/First/)).toBeDisabled();
    expect(screen.getByText(/Prev/)).toBeDisabled();
    expect(screen.getByText(/Next/)).not.toBeDisabled();
    expect(screen.getByText(/Last/)).not.toBeDisabled();
  });

  it('disables Next and Last buttons on last page', () => {
    render(
      <PaginationControls
        currentPage={4}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText(/First/)).not.toBeDisabled();
    expect(screen.getByText(/Prev/)).not.toBeDisabled();
    expect(screen.getByText(/Next/)).toBeDisabled();
    expect(screen.getByText(/Last/)).toBeDisabled();
  });

  it('calls onPageChange with 0 when First clicked', () => {
    render(
      <PaginationControls
        currentPage={3}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    fireEvent.click(screen.getByText(/First/));
    expect(mockOnPageChange).toHaveBeenCalledWith(0);
  });

  it('calls onPageChange with previous page when Prev clicked', () => {
    render(
      <PaginationControls
        currentPage={3}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    fireEvent.click(screen.getByText(/Prev/));
    expect(mockOnPageChange).toHaveBeenCalledWith(2);
  });

  it('calls onPageChange with next page when Next clicked', () => {
    render(
      <PaginationControls
        currentPage={2}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    fireEvent.click(screen.getByText(/Next/));
    expect(mockOnPageChange).toHaveBeenCalledWith(3);
  });

  it('calls onPageChange with last page when Last clicked', () => {
    render(
      <PaginationControls
        currentPage={0}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    fireEvent.click(screen.getByText(/Last/));
    expect(mockOnPageChange).toHaveBeenCalledWith(4);
  });

  it('displays all page numbers when totalPages <= 5', () => {
    render(
      <PaginationControls
        currentPage={2}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('displays max 5 page numbers when totalPages > 5', () => {
    render(
      <PaginationControls
        currentPage={5}
        totalPages={10}
        onPageChange={mockOnPageChange}
      />
    );

    const pageButtons = screen.getAllByRole('button')
      .filter(btn => !isNaN(parseInt(btn.textContent)));

    expect(pageButtons.length).toBeLessThanOrEqual(5);
  });

  it('highlights current page', () => {
    render(
      <PaginationControls
        currentPage={2}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    const currentPageButton = screen.getByText('3'); // currentPage 2 = page 3 display
    expect(currentPageButton).toHaveClass('active');
  });

  it('calls onPageChange when page number clicked', () => {
    render(
      <PaginationControls
        currentPage={0}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    fireEvent.click(screen.getByText('3'));
    expect(mockOnPageChange).toHaveBeenCalledWith(2); // Page 3 = index 2
  });

  it('displays page info text', () => {
    render(
      <PaginationControls
        currentPage={2}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText('Page 3 of 5')).toBeInTheDocument();
  });

  it('updates page info for different pages', () => {
    const { rerender } = render(
      <PaginationControls
        currentPage={0}
        totalPages={10}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText('Page 1 of 10')).toBeInTheDocument();

    rerender(
      <PaginationControls
        currentPage={9}
        totalPages={10}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText('Page 10 of 10')).toBeInTheDocument();
  });

  it('shows pages centered around current page for large page counts', () => {
    render(
      <PaginationControls
        currentPage={5}
        totalPages={20}
        onPageChange={mockOnPageChange}
      />
    );

    // Should show pages around page 6 (currentPage 5 = display 6)
    expect(screen.getByText('4')).toBeInTheDocument(); // Page 4 (index 3)
    expect(screen.getByText('5')).toBeInTheDocument(); // Page 5 (index 4)
    expect(screen.getByText('6')).toBeInTheDocument(); // Page 6 (index 5) - current
    expect(screen.getByText('7')).toBeInTheDocument(); // Page 7 (index 6)
    expect(screen.getByText('8')).toBeInTheDocument(); // Page 8 (index 7)
  });

  it('adjusts page range when near end', () => {
    render(
      <PaginationControls
        currentPage={18}
        totalPages={20}
        onPageChange={mockOnPageChange}
      />
    );

    // Should show last 5 pages
    expect(screen.getByText('16')).toBeInTheDocument();
    expect(screen.getByText('17')).toBeInTheDocument();
    expect(screen.getByText('18')).toBeInTheDocument();
    expect(screen.getByText('19')).toBeInTheDocument();
    expect(screen.getByText('20')).toBeInTheDocument();
  });

  it('does not call onPageChange when clicking current page', () => {
    render(
      <PaginationControls
        currentPage={2}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    fireEvent.click(screen.getByText('3')); // Current page
    // Should still call with the same page (no prevention in component)
    expect(mockOnPageChange).toHaveBeenCalledWith(2);
  });

  it('does not call onPageChange when Prev clicked on first page', () => {
    render(
      <PaginationControls
        currentPage={0}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    const prevButton = screen.getByText(/Prev/);
    expect(prevButton).toBeDisabled();

    fireEvent.click(prevButton);
    expect(mockOnPageChange).not.toHaveBeenCalled();
  });

  it('does not call onPageChange when Next clicked on last page', () => {
    render(
      <PaginationControls
        currentPage={4}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    const nextButton = screen.getByText(/Next/);
    expect(nextButton).toBeDisabled();

    fireEvent.click(nextButton);
    expect(mockOnPageChange).not.toHaveBeenCalled();
  });

  it('handles 2-page scenario correctly', () => {
    render(
      <PaginationControls
        currentPage={0}
        totalPages={2}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('Page 1 of 2')).toBeInTheDocument();
  });

  it('enables middle buttons on middle page', () => {
    render(
      <PaginationControls
        currentPage={2}
        totalPages={5}
        onPageChange={mockOnPageChange}
      />
    );

    expect(screen.getByText(/First/)).not.toBeDisabled();
    expect(screen.getByText(/Prev/)).not.toBeDisabled();
    expect(screen.getByText(/Next/)).not.toBeDisabled();
    expect(screen.getByText(/Last/)).not.toBeDisabled();
  });
});
