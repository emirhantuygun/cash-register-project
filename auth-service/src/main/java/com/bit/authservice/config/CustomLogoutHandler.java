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

@Log4j2
@Configuration
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;

    private Jedis jedis;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    @PostConstruct
    protected void init() {
        log.info("Entering init method in CustomLogoutHandler");
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
        log.info("Exiting init method in CustomLogoutHandler");
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        log.info("Entering logout method in CustomLogoutHandler");
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
            log.info("Exiting logout method in CustomLogoutHandler");
        }
    }
}