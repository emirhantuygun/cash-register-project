package com.bit.authservice.config;

import com.bit.authservice.entity.Token;
import com.bit.authservice.exception.InvalidAuthorizationHeaderException;
import com.bit.authservice.exception.MissingAuthorizationHeaderException;
import com.bit.authservice.exception.TokenNotFoundException;
import com.bit.authservice.repository.TokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import redis.clients.jedis.Jedis;

/**
 * CustomLogoutHandler is a class that implements LogoutHandler interface.
 * It is responsible for handling the logout process in the application.
 * It retrieves the token from the Authorization header, validates it,
 * updates the token status in the database and Redis, and logs the user out.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    private final TokenRepository tokenRepository;
    private Jedis jedis;

    /**
     * This method initializes the Jedis instance for Redis operations.
     * It is called after the bean is created and all the properties are set.
     */
    @PostConstruct
    protected void init() {
        log.trace("Entering init method in CustomLogoutHandler");
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
        log.trace("Exiting init method in CustomLogoutHandler");
    }

    /**
     * This method handles the logout process in the application.
     * It retrieves the token from the Authorization header, validates it,
     * updates the token status in the database and Redis, and logs the user out.
     *
     * @param request The HttpServletRequest object representing the incoming request.
     * @param response The HttpServletResponse object representing the outgoing response.
     * @param authentication The Authentication object representing the authenticated user.
     * @throws MissingAuthorizationHeaderException If the Authorization header is missing.
     * @throws InvalidAuthorizationHeaderException If the Authorization header format is invalid.
     * @throws TokenNotFoundException If the token is not found in the database.
     */
    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        log.trace("Entering logout method in CustomLogoutHandler");
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null) {
                log.warn("Authorization header is missing");
                throw new MissingAuthorizationHeaderException("Authorization header is missing");
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format");
                throw new InvalidAuthorizationHeaderException("Invalid Authorization header format");
            }

            String token = authHeader.substring(7);
            Token storedToken = tokenRepository.findByToken(token).orElse(null);

            if (storedToken == null) {
                log.warn("Token not found");
                throw new TokenNotFoundException("Token not found");
            }

            storedToken.setLoggedOut(true);
            tokenRepository.save(storedToken);
            jedis.set("token:" + storedToken.getId() + ":is_logged_out", "true");
            log.info("Token successfully logged out");

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw e;
        } finally {
            log.trace("Exiting logout method in CustomLogoutHandler");
        }
    }
}