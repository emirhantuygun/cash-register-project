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
class JwtUtilsTest {

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
        // Act
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));
        String username = jwtUtils.extractUsername(token);

        // Assert
        assertEquals("user", username);
    }

    @Test
    public void givenValidTokenAndUser_whenIsValid_thenReturnsTrue() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("user");

        // Act
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));

        // Assert
        assertTrue(jwtUtils.isValid(token, userDetails));
    }

    @Test
    public void givenValidToken_whenExtractClaim_thenCorrectClaimIsExtracted() {
        // Act
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));
        String username = jwtUtils.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("user", username);
    }

    @Test
    public void givenValidUserAndRoles_whenGenerateAccessToken_thenTokenIsGenerated() {
        // Act
        String token = jwtUtils.generateAccessToken("user", List.of("ROLE_USER"));

        // Assert
        assertNotNull(token);
    }

    @Test
    public void givenValidUser_whenGenerateRefreshToken_thenTokenIsGenerated() {
        // Act
        String token = jwtUtils.generateRefreshToken("user");

        // Assert
        assertNotNull(token);
    }

    @Test
    public void givenValidClaimsAndExpiration_whenBuildToken_thenTokenIsBuilt() {
        // Arrange
        Claims claims = Jwts.claims().subject("user").build();

        // Act
        String token = jwtUtils.buildToken(claims, accessTokenExpiration);

        // Assert
        assertNotNull(token);
    }

    @Test
    public void givenInvalidToken_whenExtractUsername_thenThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class, () -> jwtUtils.extractUsername(invalidToken));
    }
}
