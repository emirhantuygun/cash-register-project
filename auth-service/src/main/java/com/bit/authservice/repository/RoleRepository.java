package com.bit.authservice.repository;

import com.bit.authservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * This interface represents a repository for Role entity.
 * It extends JpaRepository which provides CRUD operations and additional methods.
 *
 * @author Emirhan Tuygun
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  /**
   * Finds a Role by its roleName.
   *
   * @param roleName the roleName of the Role to find
   * @return an Optional containing the found Role, or an empty Optional if no Role was found
   */
  Optional<Role> findByRoleName(String roleName);
}
