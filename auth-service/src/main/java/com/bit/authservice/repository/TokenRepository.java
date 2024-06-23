package com.bit.authservice.repository;

import com.bit.authservice.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * This interface represents a repository for managing {@link Token} entities.
 * It extends Spring Data JPA's {@link JpaRepository} interface, providing basic CRUD operations.
 * Additionally, it includes custom queries for retrieving tokens based on user and token value.
 */
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    /**
     * Retrieves a list of active tokens for a given user.
     *
     * @param userId the ID of the user whose tokens should be retrieved
     * @return a list of active tokens for the specified user
     */
    @Query("""
            select t from Token t inner join AppUser u on t.user.id = u.id
            where t.user.id = :userId and t.loggedOut = false
            """)
    List<Token> findAllTokensByUser(Long userId);

    /**
     * Retrieves a token by its value.
     *
     * @param token the value of the token to be retrieved
     * @return an {@link Optional} containing the token if found, or an empty {@link Optional} if not found
     */
    Optional<Token> findByToken(String token);

}
