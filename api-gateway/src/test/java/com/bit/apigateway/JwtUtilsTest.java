package com.bit.apigateway;

import com.bit.apigateway.exception.InvalidTokenException;
import com.bit.apigateway.exception.TokenNotFoundException;
import com.bit.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultJwtParser;
import io.jsonwebtoken.impl.DefaultJwtParserBuilder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Jedis jedis;

    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "SIGNER_KEY", "0eO7YVb453d57d6XdAfZ2ZCfJdl6QrXb8XE7/c4HyIs=");
        ReflectionTestUtils.setField(jwtUtils, "AUTHORITIES_KEY", "authorities");
        ReflectionTestUtils.setField(jwtUtils, "redisHost", "localhost");
        ReflectionTestUtils.setField(jwtUtils, "redisPort", "6379");

        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("0eO7YVb453d57d6XdAfZ2ZCfJdl6QrXb8XE7/c4HyIs="));

        jwtUtils.init();
    }

    @Test
    void getClaimsAndValidate_whenTokenIsValid_shouldReturnClaims() {
        // Arrange
        String token = "validToken";
        Claims claims = mock(Claims.class);
        Jws jws = mock(Jws.class);

        when(jws.getPayload()).thenReturn(claims);

        DefaultJwtParser parser = mock(DefaultJwtParser.class);
        when(parser.parseClaimsJws(token)).thenReturn(jws);

        DefaultJwtParserBuilder parserBuilder = mock(DefaultJwtParserBuilder.class);
        when(parserBuilder.verifyWith(any(SecretKey.class))).thenReturn(parserBuilder);
        when(parserBuilder.build()).thenReturn(parser);

        try (var mockedStatic = mockStatic(Jwts.class)) {
            when(Jwts.parser()).thenReturn(parserBuilder);

            // Act
            Claims result = jwtUtils.getClaimsAndValidate(token);

            // Assert
            assertEquals(claims, result);
        }
    }

    @Test
    void getClaimsAndValidate_whenTokenIsInvalid_shouldThrowInvalidTokenException() {
        // Arrange
        String token = "invalidToken";
        DefaultJwtParser parser = mock(DefaultJwtParser.class);
        when(parser.parseClaimsJws(token)).thenThrow(JwtException.class);

        DefaultJwtParserBuilder parserBuilder = mock(DefaultJwtParserBuilder.class);
        when(parserBuilder.verifyWith(any(SecretKey.class))).thenReturn(parserBuilder);
        when(parserBuilder.build()).thenReturn(parser);

        try (var mockedStatic = mockStatic(Jwts.class)) {
            when(Jwts.parser()).thenReturn(parserBuilder);

            // Act & Assert
            assertThrows(InvalidTokenException.class, () -> jwtUtils.getClaimsAndValidate(token));
        }
    }

    @Test
    void isLoggedOut_whenTokenIsLoggedOut_shouldReturnTrue() {
        // Arrange
        String token = "loggedOutToken";
        String tokenIdStr = "1";
        when(jedis.get(token)).thenReturn(tokenIdStr);
        when(jedis.get("token:1:is_logged_out")).thenReturn("true");

        // Act
        boolean result = jwtUtils.isLoggedOut(token);

        // Assert
        assertTrue(result);
    }

    @Test
    void isLoggedOut_whenTokenIsNotLoggedOut_shouldReturnFalse() {
        // Arrange
        String token = "notLoggedOutToken";
        String tokenIdStr = "1";
        when(jedis.get(token)).thenReturn(tokenIdStr);
        when(jedis.get("token:1:is_logged_out")).thenReturn("false");

        // Act
        boolean result = jwtUtils.isLoggedOut(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void isLoggedOut_whenTokenNotFoundInRedis_shouldThrowTokenNotFoundException() {
        // Arrange
        String token = "nonExistentToken";
        when(jedis.get(token)).thenReturn(null);

        // Act & Assert
        assertThrows(TokenNotFoundException.class, () -> jwtUtils.isLoggedOut(token));
    }

    @Test
    void isLoggedOut_whenLogoutStatusNotFoundInRedis_shouldThrowTokenNotFoundException() {
        // Arrange
        String token = "token";
        String tokenIdStr = "1";
        when(jedis.get(token)).thenReturn(tokenIdStr);
        when(jedis.get("token:1:is_logged_out")).thenReturn(null);

        // Act & Assert
        assertThrows(TokenNotFoundException.class, () -> jwtUtils.isLoggedOut(token));
    }

    @Test
    void getRoles_shouldReturnRolesFromClaims() {
        // Arrange
        Claims claims = mock(Claims.class);
        List<String> roles = Collections.singletonList("ROLE_USER");
        when(claims.get("authorities")).thenReturn(roles);

        // Act
        List<String> result = jwtUtils.getRoles(claims);

        // Assert
        assertEquals(roles, result);
    }
}
