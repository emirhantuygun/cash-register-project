package com.bit.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Log4j2
@Service
public class JwtUtils {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.authorities-key}")
    private String AUTHORITIES_KEY;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    public String extractUsername(String token) {
        log.trace("Entering extractUsername method in JwtUtils class");
        String username = extractClaim(token, Claims::getSubject);
        log.trace("Exiting extractUsername method in JwtUtils class with username: {}", username);
        return username;
    }

    public boolean isValid(String token, UserDetails user) {
        log.trace("Entering isValid method in JwtUtils class with token and user: {}", user.getUsername());
        String username = extractUsername(token);
        boolean isValid = (username.equals(user.getUsername())) && !isTokenExpired(token);
        log.trace("Exiting isValid method in JwtUtils class with result: {}", isValid);
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        log.trace("Entering isTokenExpired method in JwtUtils class");
        boolean isExpired = extractExpiration(token).before(new Date());
        log.trace("Exiting isTokenExpired method in JwtUtils class with result: {}", isExpired);
        return isExpired;
    }

    private Date extractExpiration(String token) {
        log.trace("Entering extractExpiration method in JwtUtils class");
        Date expiration = extractClaim(token, Claims::getExpiration);
        log.trace("Exiting extractExpiration method in JwtUtils class with expiration date: {}", expiration);
        return expiration;
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        log.trace("Entering extractClaim method in JwtUtils class");
        Claims claims = extractAllClaims(token);
        T claim = resolver.apply(claims);
        log.trace("Exiting extractClaim method in JwtUtils class with claim: {}", claim);
        return claim;
    }

    private Claims extractAllClaims(String token) {
        log.trace("Entering extractAllClaims method in JwtUtils class");
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        log.trace("Exiting extractAllClaims method in JwtUtils class");
        return claims;
    }

    public String generateAccessToken(String username, List<String> roles) {
        log.trace("Entering generateAccessToken method in JwtUtils class with username: {}", username);
        Claims claims = Jwts.claims().subject(username).add(AUTHORITIES_KEY, roles).build();
        String accessToken = buildToken(claims, ACCESS_TOKEN_EXPIRATION);
        log.trace("Exiting generateAccessToken method in JwtUtils class with accessToken: {}", accessToken);
        return accessToken;
    }

    public String generateRefreshToken(String username) {
        log.trace("Entering generateRefreshToken method in JwtUtils class with username: {}", username);
        Claims claims = Jwts.claims().subject(username).build();
        String refreshToken = buildToken(claims, REFRESH_TOKEN_EXPIRATION);
        log.trace("Exiting generateRefreshToken method in JwtUtils class with refreshToken: {}", refreshToken);
        return refreshToken;
    }

    public String buildToken(Claims claims, long expiration) {
        log.trace("Entering buildToken method in JwtUtils class with claims and expiration: {}", expiration);
        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
        log.trace("Exiting buildToken method in JwtUtils class with token: {}", token);
        return token;
    }

    private SecretKey getSignInKey() {
        log.trace("Entering getSignInKey method in JwtUtils class");
        byte[] keyBytes = Decoders.BASE64URL.decode(SIGNER_KEY);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        log.trace("Exiting getSignInKey method in JwtUtils class with key");
        return key;
    }
}
