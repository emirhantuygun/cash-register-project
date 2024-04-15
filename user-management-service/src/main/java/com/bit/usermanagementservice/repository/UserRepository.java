package com.bit.usermanagementservice.repository;

import com.bit.usermanagementservice.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    @Query(value = "SELECT * FROM users WHERE deleted = true", nativeQuery = true)
    List<AppUser> findSoftDeletedUsers();

    @Query(value = "SELECT CASE WHEN deleted = true THEN true ELSE false END FROM users WHERE id = :id", nativeQuery = true)
    boolean isUserSoftDeleted(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreUser(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
