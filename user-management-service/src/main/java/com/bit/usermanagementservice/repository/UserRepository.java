package com.bit.usermanagementservice.repository;

import com.bit.usermanagementservice.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This interface represents the User Repository, which extends the JpaRepository interface.
 * It provides methods for interacting with the AppUser entity in the database.
 *
 * @author Emirhan Tuygun
 */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * This method retrieves a paginated list of AppUser entities based on the given Specification and Pageable.
     *
     * @param spec The Specification to filter the AppUser entities.
     * @param pageable The Pageable to define the pagination parameters.
     * @return A Page of AppUser entities that match the given Specification and Pageable.
     */
    Page<AppUser> findAll(Specification<AppUser> spec, Pageable pageable);

    /**
     * This method retrieves a list of soft-deleted AppUser entities from the database.
     *
     * @return A List of soft-deleted AppUser entities.
     */
    @Query(value = "SELECT * FROM users WHERE deleted = true", nativeQuery = true)
    List<AppUser> findSoftDeletedUsers();

    /**
     * This method checks if an AppUser entity with the given ID exists and is soft-deleted.
     *
     * @param id The ID of the AppUser entity to check.
     * @return True if an AppUser entity with the given ID exists and is soft-deleted, otherwise false.
     */
    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM users u WHERE u.id = :id AND u.deleted = true", nativeQuery = true)
    boolean existsByIdAndDeletedTrue(@Param("id") Long id);

    /**
     * This method soft-deletes an AppUser entity with the given ID.
     *
     * @param id The ID of the AppUser entity to soft-delete.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET deleted = true WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") Long id);

    /**
     * This method restores a soft-deleted AppUser entity with the given ID.
     *
     * @param id The ID of the AppUser entity to restore.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreUser(@Param("id") Long id);

    /**
     * This method permanently deletes an AppUser entity with the given ID.
     *
     * @param id The ID of the AppUser entity to delete permanently.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

    /**
     * This method deletes all the roles associated with an AppUser entity with the given ID.
     *
     * @param id The ID of the AppUser entity to delete the roles for.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :id", nativeQuery = true)
    void deleteRolesForUser(@Param("id") Long id);

    /**
     * This method checks if an AppUser entity with the given username exists.
     *
     * @param username The username to check.
     * @return True if an AppUser entity with the given username exists, otherwise false.
     */
    boolean existsByUsername(String username);

    /**
     * This method checks if an AppUser entity with the given email exists.
     *
     * @param email The email to check.
     * @return True if an AppUser entity with the given email exists, otherwise false.
     */
    boolean existsByEmail(String email);
}
