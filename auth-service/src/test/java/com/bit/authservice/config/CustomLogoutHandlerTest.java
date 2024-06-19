package com.bit.authservice.config;

import com.bit.authservice.exception.InvalidAuthorizationHeaderException;
import com.bit.authservice.exception.MissingAuthorizationHeaderException;
import com.bit.authservice.exception.TokenNotFoundException;
import com.bit.authservice.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomLogoutHandlerTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomLogoutHandler customLogoutHandler;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(customLogoutHandler, "redisHost", "localhost");
        ReflectionTestUtils.setField(customLogoutHandler, "redisPort", "6379");
    }

    @Test
    public void givenValidRedisHostAndPort_whenInitMethodIsCalled_thenJedisInstanceIsInitialized() {
        // Act
        customLogoutHandler.init();
        Jedis jedis = (Jedis) ReflectionTestUtils.getField(customLogoutHandler, "jedis");

        // Assert
        assertNotNull(jedis, "Jedis instance should be initialized");
    }

    @Test
    public void givenInvalidPort_whenInitMethodIsCalled_thenNumberFormatExceptionIsThrown() {
        // Arrange
        ReflectionTestUtils.setField(customLogoutHandler, "redisPort", "invalidPort");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> customLogoutHandler.init(), "Invalid port should throw NumberFormatException");
    }

    @Test
    public void testLogout_WhenAuthorizationHeaderIsMissing_ShouldThrowMissingAuthorizationHeaderException() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act & Assert
        assertThrows(MissingAuthorizationHeaderException.class, () ->
            customLogoutHandler.logout(request, response, authentication)
        );
    }

    @Test
    public void testLogout_WhenAuthorizationHeaderIsInvalid_ShouldThrowInvalidAuthorizationHeaderException() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        // Act & Assert
        assertThrows(InvalidAuthorizationHeaderException.class, () ->
            customLogoutHandler.logout(request, response, authentication)
        );
    }

    @Test
    public void testLogout_WhenTokenNotFound_ShouldThrowTokenNotFoundException() {
        // Arrange
        String token = "Bearer validToken";
        when(request.getHeader("Authorization")).thenReturn(token);
        when(tokenRepository.findByToken("validToken")).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(TokenNotFoundException.class, () ->
            customLogoutHandler.logout(request, response, authentication)
        );
    }
}
