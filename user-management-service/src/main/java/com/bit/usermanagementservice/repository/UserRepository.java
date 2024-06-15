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

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Page<AppUser> findAll(Specification<AppUser> spec, Pageable pageable);

    @Query(value = "SELECT * FROM users WHERE deleted = true", nativeQuery = true)
    List<AppUser> findSoftDeletedUsers();

    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM users u WHERE u.id = :id AND u.deleted = true", nativeQuery = true)
    boolean existsByIdAndDeletedTrue(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET deleted = true WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreUser(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :id", nativeQuery = true)
    void deleteRolesForUser(@Param("id") Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
