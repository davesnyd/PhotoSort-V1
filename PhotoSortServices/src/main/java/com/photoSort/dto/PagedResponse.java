/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper for paginated API responses.
 * Matches the pagination format specified in PhotoSpecification.md.
 *
 * @param <T> Type of content in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int pageSize;
    private int totalPages;
    private long totalElements;

    /**
     * Factory method to create PagedResponse from Spring Data Page.
     *
     * @param page Spring Data Page object
     * @param <T> Type of content
     * @return PagedResponse
     */
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        PagedResponse<T> response = new PagedResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        return response;
    }
}
