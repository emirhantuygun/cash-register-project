package com.bit.apigateway.util;

import com.bit.apigateway.exception.InvalidTokenException;
import com.bit.apigateway.exception.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Utility class for handling JWT (JSON Web Tokens) and Redis operations.
 * This class provides methods for validating JWT tokens, checking if a token is logged out,
 * and retrieving user roles from the token claims.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Component
public class JwtUtils {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.authorities-key}")
    private String AUTHORITIES_KEY;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;
    private Jedis jedis;

    /**
     * This method initializes the Jedis instance for Redis operations.
     * It is annotated with {@link PostConstruct} to ensure it is called after all dependencies are injected.
     *
     * @throws NumberFormatException If the redisPort value cannot be parsed to an integer.
     */
    @PostConstruct
    public void init() {
        log.trace("Entering init method in JwtUtils");
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
        log.trace("Exiting init method in JwtUtils");
    }

    /**
     * This method verifies and parses the given JWT token.
     *
     * @param token The JWT token to be validated and parsed.
     * @return The claims extracted from the validated token.
     * @throws InvalidTokenException If the token is invalid or cannot be parsed.
     */
    public Claims getClaimsAndValidate(String token) {
        log.trace("Entering getClaimsAndValidate method in JwtUtils");
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error while parsing token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        } finally {
            log.trace("Exiting getClaimsAndValidate method in JwtUtils");
        }
    }

    /**
     * This method checks if the given JWT token is logged out.
     * It retrieves the token ID from Redis and checks if the corresponding logout status is stored in Redis.
     *
     * @param token The JWT token to be checked.
     * @return {@code true} if the token is logged out, {@code false} otherwise.
     * @throws TokenNotFoundException If the token is not found in Redis or if the logout status information is not found.
     */
    public boolean isLoggedOut(String token) {
        log.trace("Entering isLoggedOut method in JwtUtils");
        try {
            // Getting token ID
            String tokenIdStr = jedis.get(token);
            if (tokenIdStr == null) {
                log.error("Token not found in Redis");
                throw new TokenNotFoundException("Token not found in Redis");
            }
            log.debug("Token ID: " + tokenIdStr);

            long tokenId = Long.parseLong(tokenIdStr);
            String key = "token:" + tokenId + ":is_logged_out";

            // Getting logout status
            String value = jedis.get(key);
            if (value == null) {
                log.error("Token does not have logged out information record in Redis");
                throw new TokenNotFoundException("Token's logout status information not found in Redis");
            }
            log.debug("Token's logout status information: " + value);

            return Boolean.parseBoolean(value);
        } finally {
            log.trace("Exiting isLoggedOut method in JwtUtils");
        }
    }

    /**
     * Retrieves the roles from the given JWT claims.
     *
     * @param claims The JWT claims containing the roles information.
     * @return A list of roles extracted from the claims.
     */
    public List<String> getRoles(Claims claims) {
        log.trace("Entering getRoles method in JwtUtils");
        try {
            return (List<String>) claims.get(AUTHORITIES_KEY);
        } finally {
            log.trace("Exiting getRoles method in JwtUtils");
        }
    }

    /**
     * This method retrieves the secret key used for signing and validating JWT tokens.
     * The key is obtained from the {@code jwt.signerKey} property and is decoded using the BASE64URL decoder.
     * The decoded key is then used to create a secret key using the HMAC SHA algorithm.
     *
     * @return The secret key used for signing and validating JWT tokens.
     */
    protected SecretKey getSignInKey() {
        log.trace("Entering getSignInKey method in JwtUtils");
        try {
            byte[] keyBytes = Decoders.BASE64URL.decode(SIGNER_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
        } finally {
            log.trace("Exiting getSignInKey method in JwtUtils");
        }
    }
}