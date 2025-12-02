/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.photoSort.dto.PhotoDTO;
import com.photoSort.model.Photo;
import com.photoSort.model.PhotoPermission;
import com.photoSort.model.User;
import com.photoSort.repository.PhotoPermissionRepository;
import com.photoSort.repository.PhotoRepository;
import com.photoSort.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing photos with permission-based filtering.
 * Implements complex query logic for photo access control.
 */
@Service
@Transactional
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoPermissionRepository photoPermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get photos accessible by a user with filtering, sorting, and pagination.
     *
     * Users can see:
     * - Photos they own
     * - Public photos
     * - Private photos they have explicit permission for
     *
     * @param user Current user
     * @param pageable Pagination and sorting parameters
     * @param search Quick search term (searches fileName and filePath)
     * @param filterField1 Advanced filter field 1
     * @param filterValue1 Advanced filter value 1
     * @param filterType1 Advanced filter type 1 (MUST_CONTAIN or MUST_NOT_CONTAIN)
     * @param filterField2 Advanced filter field 2
     * @param filterValue2 Advanced filter value 2
     * @param filterType2 Advanced filter type 2
     * @return Page of PhotoDTOs
     */
    public Page<PhotoDTO> getPhotosForUser(User user, Pageable pageable, String search,
                                            String filterField1, String filterValue1, String filterType1,
                                            String filterField2, String filterValue2, String filterType2) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Get permitted photo IDs once
        List<PhotoPermission> permissions = photoPermissionRepository.findByUser(user);
        List<Long> permittedPhotoIds = permissions.stream()
                .map(p -> p.getPhoto().getPhotoId())
                .collect(Collectors.toList());

        // Main query
        CriteriaQuery<Photo> query = cb.createQuery(Photo.class);
        Root<Photo> photo = query.from(Photo.class);
        List<Predicate> predicates = buildUserPredicates(cb, photo, user, permittedPhotoIds, search,
                filterField1, filterValue1, filterType1, filterField2, filterValue2, filterType2);
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(photo.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(photo.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // Execute query with pagination
        List<Photo> photos = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count total with separate predicates
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Photo> countRoot = countQuery.from(Photo.class);
        List<Predicate> countPredicates = buildUserPredicates(cb, countRoot, user, permittedPhotoIds, search,
                filterField1, filterValue1, filterType1, filterField2, filterValue2, filterType2);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        // Convert to DTOs
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(PhotoDTO::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(photoDTOs, pageable, total);
    }

    /**
     * Build predicates for user permission filtering.
     */
    private List<Predicate> buildUserPredicates(CriteriaBuilder cb, Root<Photo> photo, User user,
                                                 List<Long> permittedPhotoIds, String search,
                                                 String filterField1, String filterValue1, String filterType1,
                                                 String filterField2, String filterValue2, String filterType2) {
        List<Predicate> predicates = new ArrayList<>();

        // Permission filtering: owned OR public OR explicitly granted
        Predicate ownedByUser = cb.equal(photo.get("owner").get("userId"), user.getUserId());
        Predicate isPublic = cb.isTrue(photo.get("isPublic"));
        Predicate hasPermission = permittedPhotoIds.isEmpty()
                ? cb.disjunction() // Always false if no permissions
                : photo.get("photoId").in(permittedPhotoIds);

        predicates.add(cb.or(ownedByUser, isPublic, hasPermission));

        // Quick search
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            Predicate fileNameMatch = cb.like(cb.lower(photo.get("fileName")), searchPattern);
            Predicate filePathMatch = cb.like(cb.lower(photo.get("filePath")), searchPattern);
            predicates.add(cb.or(fileNameMatch, filePathMatch));
        }

        // Advanced filters
        addAdvancedFilter(cb, photo, predicates, filterField1, filterValue1, filterType1);
        addAdvancedFilter(cb, photo, predicates, filterField2, filterValue2, filterType2);

        return predicates;
    }

    /**
     * Get all photos (admin view) with optional user filtering.
     *
     * @param userId Optional user ID to filter by owner
     * @param pageable Pagination and sorting parameters
     * @param search Quick search term
     * @param filterField1 Advanced filter field 1
     * @param filterValue1 Advanced filter value 1
     * @param filterType1 Advanced filter type 1
     * @param filterField2 Advanced filter field 2
     * @param filterValue2 Advanced filter value 2
     * @param filterType2 Advanced filter type 2
     * @return Page of PhotoDTOs
     */
    public Page<PhotoDTO> getPhotosForAdmin(Long userId, Pageable pageable, String search,
                                             String filterField1, String filterValue1, String filterType1,
                                             String filterField2, String filterValue2, String filterType2) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query
        CriteriaQuery<Photo> query = cb.createQuery(Photo.class);
        Root<Photo> photo = query.from(Photo.class);
        List<Predicate> predicates = buildAdminPredicates(cb, photo, userId, search,
                filterField1, filterValue1, filterType1, filterField2, filterValue2, filterType2);
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(photo.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(photo.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        // Execute query with pagination
        List<Photo> photos = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count total with separate predicates
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Photo> countRoot = countQuery.from(Photo.class);
        List<Predicate> countPredicates = buildAdminPredicates(cb, countRoot, userId, search,
                filterField1, filterValue1, filterType1, filterField2, filterValue2, filterType2);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        // Convert to DTOs
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(PhotoDTO::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(photoDTOs, pageable, total);
    }

    /**
     * Build predicates for admin filtering (no permission restrictions).
     */
    private List<Predicate> buildAdminPredicates(CriteriaBuilder cb, Root<Photo> photo, Long userId, String search,
                                                  String filterField1, String filterValue1, String filterType1,
                                                  String filterField2, String filterValue2, String filterType2) {
        List<Predicate> predicates = new ArrayList<>();

        // Filter by user if specified
        if (userId != null) {
            predicates.add(cb.equal(photo.get("owner").get("userId"), userId));
        }

        // Quick search
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            Predicate fileNameMatch = cb.like(cb.lower(photo.get("fileName")), searchPattern);
            Predicate filePathMatch = cb.like(cb.lower(photo.get("filePath")), searchPattern);
            predicates.add(cb.or(fileNameMatch, filePathMatch));
        }

        // Advanced filters
        addAdvancedFilter(cb, photo, predicates, filterField1, filterValue1, filterType1);
        addAdvancedFilter(cb, photo, predicates, filterField2, filterValue2, filterType2);

        return predicates;
    }

    /**
     * Add an advanced filter to the query predicates.
     *
     * @param cb CriteriaBuilder
     * @param photo Photo root
     * @param predicates List of predicates to add to
     * @param field Field name to filter on
     * @param value Value to filter for
     * @param type Filter type (MUST_CONTAIN or MUST_NOT_CONTAIN)
     */
    private void addAdvancedFilter(CriteriaBuilder cb, Root<Photo> photo,
                                    List<Predicate> predicates,
                                    String field, String value, String type) {
        if (field != null && !field.trim().isEmpty() &&
            value != null && !value.trim().isEmpty() &&
            type != null) {

            String searchPattern = "%" + value.toLowerCase() + "%";
            Predicate fieldMatch = cb.like(cb.lower(photo.get(field).as(String.class)), searchPattern);

            if ("MUST_NOT_CONTAIN".equals(type)) {
                predicates.add(cb.not(fieldMatch));
            } else if ("MUST_CONTAIN".equals(type)) {
                predicates.add(fieldMatch);
            }
        }
    }
}
