/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.photoSort.dto.PagedResponse;
import com.photoSort.dto.SearchFilterDTO;
import com.photoSort.dto.UserDTO;
import com.photoSort.model.User;
import com.photoSort.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing user authentication and user-related operations.
 * Handles OAuth user creation and login tracking.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Process OAuth login. Creates new user on first login or updates last login date
     * for returning users.
     *
     * @param googleId     Google OAuth user ID
     * @param email        User's email address
     * @param displayName  User's display name
     * @return The user (newly created or existing)
     */
    public User processOAuthLogin(String googleId, String email, String displayName) {
        logger.info("processOAuthLogin called - googleId: {}, email: {}", googleId, email);

        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            // Returning user - update last login date
            User user = existingUser.get();
            user.setLastLoginDate(LocalDateTime.now());
            User savedUser = userRepository.save(user);
            logger.info("Existing user updated - userId: {}", savedUser.getUserId());
            return savedUser;
        } else {
            // New user - create account
            logger.info("Creating new user - email: {}", email);
            User newUser = new User();
            newUser.setGoogleId(googleId);
            newUser.setEmail(email);
            newUser.setDisplayName(displayName);
            newUser.setUserType(User.UserType.USER); // Default to regular user
            newUser.setFirstLoginDate(LocalDateTime.now());
            newUser.setLastLoginDate(LocalDateTime.now());
            User savedUser = userRepository.save(newUser);
            logger.info("New user created - userId: {}, email: {}", savedUser.getUserId(), savedUser.getEmail());
            return savedUser;
        }
    }

    /**
     * Find a user by Google ID.
     *
     * @param googleId Google OAuth user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    /**
     * Find a user by email address.
     *
     * @param email User's email
     * @return Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get a user by ID.
     *
     * @param userId User's ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Update a user's type (USER or ADMIN).
     * Only administrators should be able to call this method.
     *
     * @param userId   User's ID
     * @param userType New user type
     * @return Updated user
     * @throws IllegalArgumentException if user not found
     */
    public User updateUserType(Long userId, User.UserType userType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setUserType(userType);
        return userRepository.save(user);
    }

    /**
     * Check if a user is an administrator.
     *
     * @param userId User's ID
     * @return true if user is an admin, false otherwise
     */
    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUserType() == User.UserType.ADMIN)
                .orElse(false);
    }

    /**
     * Get paginated list of users with photo counts.
     * Uses optimized JOIN query to avoid N+1 problem.
     *
     * @param page     Page number (0-indexed)
     * @param pageSize Number of items per page
     * @param sortBy   Field to sort by (e.g., "email", "displayName", "userType")
     * @param sortDir  Sort direction ("asc" or "desc")
     * @return Paginated response with user DTOs including photo counts
     */
    public PagedResponse<UserDTO> getUsers(int page, int pageSize, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        // Get users with photo counts using optimized JOIN query
        List<Object[]> results = userRepository.findAllWithPhotoCounts(pageable);

        // Convert to DTOs
        List<UserDTO> userDTOs = results.stream()
                .map(result -> {
                    User user = (User) result[0];
                    Long photoCount = (Long) result[1];
                    return UserDTO.fromUser(user, photoCount);
                })
                .collect(Collectors.toList());

        // Get total count for pagination
        long totalElements = userRepository.countAllUsers();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return new PagedResponse<>(userDTOs, page, pageSize, totalPages, totalElements);
    }

    /**
     * Quick search users by email or display name with photo counts.
     *
     * @param searchTerm Search term to match against email or display name
     * @param page       Page number (0-indexed)
     * @param pageSize   Number of items per page
     * @param sortBy     Field to sort by
     * @param sortDir    Sort direction ("asc" or "desc")
     * @return Paginated response with matching user DTOs
     */
    public PagedResponse<UserDTO> searchUsers(String searchTerm, int page, int pageSize,
                                              String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        // Get matching users with photo counts
        List<Object[]> results = userRepository.searchUsersWithPhotoCounts(searchTerm, pageable);

        // Convert to DTOs
        List<UserDTO> userDTOs = results.stream()
                .map(result -> {
                    User user = (User) result[0];
                    Long photoCount = (Long) result[1];
                    return UserDTO.fromUser(user, photoCount);
                })
                .collect(Collectors.toList());

        // Get total count for pagination
        long totalElements = userRepository.countSearchResults(searchTerm);
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return new PagedResponse<>(userDTOs, page, pageSize, totalPages, totalElements);
    }

    /**
     * Advanced search users with multiple filter criteria.
     * Filters are combined with AND logic.
     *
     * @param filters  List of search filters
     * @param page     Page number (0-indexed)
     * @param pageSize Number of items per page
     * @param sortBy   Field to sort by
     * @param sortDir  Sort direction ("asc" or "desc")
     * @return Paginated response with matching user DTOs
     */
    public PagedResponse<UserDTO> advancedSearchUsers(List<SearchFilterDTO> filters, int page,
                                                      int pageSize, String sortBy, String sortDir) {
        // Build dynamic specification from filters
        Specification<User> spec = buildSpecification(filters);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        // Get matching users using specification
        var userPage = userRepository.findAll(spec, pageable);

        // Convert to DTOs with photo counts
        // Note: This approach does individual queries per user. For better performance,
        // consider adding a custom repository method with JOIN if this becomes a bottleneck.
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(user -> {
                    // For now, set photo count to 0 or query individually
                    // TODO: Optimize with batch query if needed
                    return UserDTO.fromUser(user, 0L);
                })
                .collect(Collectors.toList());

        return new PagedResponse<>(userDTOs, page, (int) userPage.getSize(),
                                    userPage.getTotalPages(), userPage.getTotalElements());
    }

    /**
     * Build JPA Specification from search filters.
     * Combines multiple filters with AND logic.
     *
     * @param filters List of search filters
     * @return JPA Specification for dynamic query
     */
    private Specification<User> buildSpecification(List<SearchFilterDTO> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (SearchFilterDTO filter : filters) {
                String column = filter.getColumn();
                String value = filter.getValue();
                SearchFilterDTO.FilterOperation operation = filter.getOperation();

                if (value == null || value.trim().isEmpty()) {
                    continue; // Skip empty filters
                }

                Predicate predicate = null;

                switch (column) {
                    case "displayName":
                    case "email":
                        if (operation == SearchFilterDTO.FilterOperation.CONTAINS) {
                            predicate = criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(column)),
                                "%" + value.toLowerCase() + "%"
                            );
                        } else { // NOT_CONTAINS
                            predicate = criteriaBuilder.notLike(
                                criteriaBuilder.lower(root.get(column)),
                                "%" + value.toLowerCase() + "%"
                            );
                        }
                        break;

                    case "userType":
                        if (operation == SearchFilterDTO.FilterOperation.CONTAINS) {
                            predicate = criteriaBuilder.like(
                                root.get(column).as(String.class),
                                "%" + value.toUpperCase() + "%"
                            );
                        } else { // NOT_CONTAINS
                            predicate = criteriaBuilder.notLike(
                                root.get(column).as(String.class),
                                "%" + value.toUpperCase() + "%"
                            );
                        }
                        break;

                    case "firstLoginDate":
                    case "lastLoginDate":
                        // For date fields, treat value as string to match against formatted date
                        predicate = criteriaBuilder.like(
                            root.get(column).as(String.class),
                            "%" + value + "%"
                        );
                        if (operation == SearchFilterDTO.FilterOperation.NOT_CONTAINS) {
                            predicate = criteriaBuilder.not(predicate);
                        }
                        break;

                    default:
                        // Unknown column, skip
                        break;
                }

                if (predicate != null) {
                    predicates.add(predicate);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
