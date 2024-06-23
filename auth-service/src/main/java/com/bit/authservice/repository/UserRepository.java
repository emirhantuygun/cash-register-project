package com.bit.authservice.repository;

import com.bit.authservice.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * This interface represents a repository for managing {@link AppUser} entities.
 * It extends Spring Data JPA's {@link JpaRepository} interface, providing basic CRUD operations.
 * Additionally, it includes custom methods for deleting, restoring, and permanently deleting users,
 * as well as deleting user roles.
 *
 * @author Emirhan Tuygun
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Finds an {@link AppUser} by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the {@link AppUser} if found, otherwise {@link Optional#empty()}
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Marks the user with the given ID as deleted.
     *
     * @param id the ID of the user to delete
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET deleted = true WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") Long id);

    /**
     * Restores the user with the given ID.
     *
     * @param id the ID of the user to restore
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreUser(@Param("id") Long id);

    /**
     * Permanently deletes the user with the given ID.
     *
     * @param id the ID of the user to delete permanently
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

    /**
     * Deletes all roles associated with the user with the given ID.
     *
     * @param id the ID of the user whose roles to delete
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :id", nativeQuery = true)
    void deleteRolesForUser(@Param("id") Long id);
}
