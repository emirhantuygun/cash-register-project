package com.bit.apigateway;

import com.bit.apigateway.exception.InvalidTokenException;
import com.bit.apigateway.exception.TokenNotFoundException;
import com.bit.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
import io.jsonwebtoken.Jwts;


@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Jedis jedis;


    @BeforeEach
    void setUp() {
    }

//    @Test
//    void getClaimsAndValidate_whenTokenIsValid_shouldReturnClaims() {
//        // Arrange
//        String token = "validToken";
//
//        Claims claims = mock();
//        JwtParserBuilder parserBuilder = mock();
//        JwtParser parser = mock();
//        Jws<Claims> jws = mock();
//
//        Mockito.lenient().when(parserBuilder.verifyWith(any(SecretKey.class))).thenReturn(parserBuilder);
//        when(parserBuilder.build()).thenReturn(parser);
//        when(parser.parseSignedClaims(anyString())).thenReturn(jws);
//        when(jws.getPayload()).thenReturn(claims);
//
//        // Act
//        Claims result = jwtUtils.getClaimsAndValidate(token);
//
//        // Assert
//        assertEquals(claims, result);
//    }


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
