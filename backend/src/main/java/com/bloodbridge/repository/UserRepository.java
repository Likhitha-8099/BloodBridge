package com.bloodbridge.repository;

import com.bloodbridge.entity.User;
import com.bloodbridge.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link User} entity.
 * Provides data access operations for users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the specified email already exists.
     *
     * @param email the email to check
     * @return true if a user exists with the given email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds users by role.
     *
     * @param role target role
     * @return List of User entities
     */
    List<User> findByRole(Role role);

    /**
     * Finds users by role with pagination.
     *
     * @param role target role
     * @param pageable page request
     * @return Page of User entities
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Finds users by searching full name or email (case-insensitive) with pagination.
     *
     * @param name search term for name
     * @param email search term for email
     * @param pageable page request
     * @return Page of User entities
     */
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    /**
     * Counts users with a specific role.
     *
     * @param role the user role
     * @return count of users
     */
    long countByRole(Role role);

    /**
     * Counts users with a specific active status.
     *
     * @param active active status
     * @return count of users
     */
    long countByActive(Boolean active);

    /**
     * Finds users with dynamic search, city, state filtering and pagination.
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:city IS NULL OR :city = '' OR LOWER(u.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR :state = '' OR LOWER(u.state) LIKE LOWER(CONCAT('%', :state, '%')))")
    Page<User> findUsersWithFilters(@org.springframework.data.repository.query.Param("search") String search,
                                   @org.springframework.data.repository.query.Param("city") String city,
                                   @org.springframework.data.repository.query.Param("state") String state,
                                   Pageable pageable);
}
