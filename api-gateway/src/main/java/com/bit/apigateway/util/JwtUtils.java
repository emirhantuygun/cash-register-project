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

    @PostConstruct
    public void init() {
        log.info("Entering init method in JwtUtils");
        try {
            this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));
        } finally {
            log.info("Exiting init method in JwtUtils");
        }
    }

    public Claims getClaimsAndValidate(String token) {
        log.info("Entering getClaimsAndValidate method in JwtUtils");
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error while parsing token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        } finally {
            log.info("Exiting getClaimsAndValidate method in JwtUtils");
        }
    }

    public boolean isLoggedOut(String token) {
        log.info("Entering isLoggedOut method in JwtUtils");
        try {
            String tokenIdStr = jedis.get(token);
            if (tokenIdStr == null) {
                log.error("Token not found in Redis");
                throw new TokenNotFoundException("Token not found in Redis");
            }

            long tokenId = Long.parseLong(tokenIdStr);
            String key = "token:" + tokenId + ":is_logged_out";

            String value = jedis.get(key);
            if (value == null) {
                log.error("Token does not have logged out information record in Redis");
                throw new TokenNotFoundException("Token's logout status information not found in Redis");
            }

            return Boolean.parseBoolean(value);
        } finally {
            log.info("Exiting isLoggedOut method in JwtUtils");
        }
    }

    public List<String> getRoles(Claims claims) {
        log.info("Entering getRoles method in JwtUtils");
        try {
            return (List<String>) claims.get(AUTHORITIES_KEY);
        } finally {
            log.info("Exiting getRoles method in JwtUtils");
        }
    }

    protected SecretKey getSignInKey() {
        log.info("Entering getSignInKey method in JwtUtils");
        try {
            byte[] keyBytes = Decoders.BASE64URL.decode(SIGNER_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
        } finally {
            log.info("Exiting getSignInKey method in JwtUtils");
        }
    }
}