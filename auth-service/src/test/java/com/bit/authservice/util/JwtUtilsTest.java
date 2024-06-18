package com.bit.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private UserDetails userDetails;

    private final long accessTokenExpiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    public void setUp() {
        final String signerKey = "testsignerkeytestsignerkeytestsignerkeytestsignerkey";
        final String authoritiesKey = "roles";
        final long refreshTokenExpiration = 1000 * 60 * 60 * 24; // 24 hours
        ReflectionTestUtils.setField(jwtUtils, "SIGNER_KEY", signerKey);
        ReflectionTestUtils.setField(jwtUtils, "AUTHORITIES_KEY", authoritiesKey);
        ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_EXPIRATION", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtUtils, "REFRESH_TOKEN_EXPIRATION", refreshTokenExpiration);
    }

    @Test
    public void givenValidToken_whenExtractUsername_thenUsernameIsExtracted() {
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));
        String username = jwtUtils.extractUsername(token);
        assertEquals("user", username);
    }

    @Test
    public void givenValidTokenAndUser_whenIsValid_thenReturnsTrue() {
        when(userDetails.getUsername()).thenReturn("user");
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));
        assertTrue(jwtUtils.isValid(token, userDetails));
    }

//    @Test
//    public void givenExpiredToken_whenIsTokenExpired_thenReturnsTrue() {
//        String token = jwtUtils.generateAccessToken("user", Arrays.asList("ROLE_USER"));
//        // Simulate token expiration by setting a past expiration time
//        ReflectionTestUtils.setField(jwtUtils, "ACCESS_TOKEN_EXPIRATION", -1000);
//        assertTrue(jwtUtils.isTokenExpired(token));
//    }
//
//    @Test
//    public void givenValidToken_whenExtractExpiration_thenCorrectExpirationDateIsExtracted() {
//        String token = jwtUtils.generateAccessToken("user", Arrays.asList("ROLE_USER"));
//        Date expiration = jwtUtils.extractExpiration(token);
//        assertNotNull(expiration);
//    }

    @Test
    public void givenValidToken_whenExtractClaim_thenCorrectClaimIsExtracted() {
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));
        String username = jwtUtils.extractClaim(token, Claims::getSubject);
        assertEquals("user", username);
    }

//    @Test
//    public void givenValidToken_whenExtractAllClaims_thenAllClaimsAreExtracted() {
//        String token = jwtUtils.generateAccessToken("user", Arrays.asList("ROLE_USER"));
//        Claims claims = jwtUtils.extractAllClaims(token);
//        assertEquals("user", claims.getSubject());
//    }

    @Test
    public void givenValidUserAndRoles_whenGenerateAccessToken_thenTokenIsGenerated() {
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));
        assertNotNull(token);
    }

    @Test
    public void givenValidUser_whenGenerateRefreshToken_thenTokenIsGenerated() {
        String token = jwtUtils.generateRefreshToken("user");
        assertNotNull(token);
    }

    @Test
    public void givenValidClaimsAndExpiration_whenBuildToken_thenTokenIsBuilt() {
        Claims claims = Jwts.claims().subject("user").build();
        String token = jwtUtils.buildToken(claims, accessTokenExpiration);
        assertNotNull(token);
    }

//    @Test
//    public void givenValidSignerKey_whenGetSignInKey_thenKeyIsRetrieved() {
//        SecretKey key = jwtUtils.getSignInKey();
//        assertNotNull(key);
//    }

    @Test
    public void givenInvalidToken_whenExtractUsername_thenThrowsException() {
        String invalidToken = "invalid.token.here";
        assertThrows(MalformedJwtException.class, () -> jwtUtils.extractUsername(invalidToken));
    }
}
