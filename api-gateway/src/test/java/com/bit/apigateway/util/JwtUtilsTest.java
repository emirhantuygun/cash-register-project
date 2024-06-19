package com.bit.apigateway.util;

import com.bit.apigateway.exception.InvalidTokenException;
import com.bit.apigateway.exception.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @Mock
    private Jedis jedis;
    @InjectMocks
    private JwtUtils jwtUtils;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "SIGNER_KEY", "4bb6d1dfbafb64a681139d1586b6f1160d18159afd57c8c79136d7490630407c");
        ReflectionTestUtils.setField(jwtUtils, "redisHost", "localhost");
        ReflectionTestUtils.setField(jwtUtils, "redisPort", "6379");
    }

    @Test
    public void givenValidRedisHostAndPort_whenInitMethodIsCalled_thenJedisInstanceIsInitialized() {
        // Act
        jwtUtils.init();
        Jedis jedis = (Jedis) ReflectionTestUtils.getField(jwtUtils, "jedis");

        // Assert
        assertNotNull(jedis, "Jedis instance should be initialized");
    }

    @Test
    public void givenInvalidPort_whenInitMethodIsCalled_thenNumberFormatExceptionIsThrown() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtils, "redisPort", "invalidPort");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> jwtUtils.init(), "Invalid port should throw NumberFormatException");
    }

    @Test
    void testGetClaimsAndValidate_ShouldThrowInvalidTokenException_WhenTokenIsNull() {

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> jwtUtils.getClaimsAndValidate(null));

        assertEquals("Invalid token", exception.getMessage());
    }


    @Test
    void getClaimsAndValidate_whenTokenIsInvalid_shouldThrowInvalidTokenException() {
        // Arrange
        String token = "invalidToken";

        JwtParserBuilder parserBuilder = mock(JwtParserBuilder.class);
        JwtParser parser = mock(JwtParser.class);
        Mockito.lenient().when(parserBuilder.verifyWith(any(SecretKey.class))).thenReturn(parserBuilder);
        Mockito.lenient().when(parserBuilder.build()).thenReturn(parser);
        Mockito.lenient().when(parser.parseSignedClaims(anyString())).thenThrow(InvalidTokenException.class);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtUtils.getClaimsAndValidate(token));
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
        when(jedis.get(anyString())).thenReturn(null);

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
        ReflectionTestUtils.setField(jwtUtils, "AUTHORITIES_KEY", "authorities");
        Claims claims = mock(Claims.class);
        List<String> roles = Collections.singletonList("ROLE_USER");
        when(claims.get("authorities")).thenReturn(roles);

        // Act
        List<String> result = jwtUtils.getRoles(claims);

        // Assert
        assertEquals(roles, result);
    }
}
