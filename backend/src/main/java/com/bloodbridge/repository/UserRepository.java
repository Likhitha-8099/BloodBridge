package com.bloodbridge.repository;

import com.bloodbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * @return an {@link Optional} containing the user if found, or empty otherwise
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
     * Counts users with a specific role.
     *
     * @param role the user role
     * @return count of users
     */
    long countByRole(com.bloodbridge.enums.Role role);

    /**
     * Counts users with a specific active status.
     *
     * @param active active status
     * @return count of users
     */
    long countByActive(Boolean active);
}
