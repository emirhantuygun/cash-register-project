package com.bit.authservice.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bit.authservice.config.CustomLogoutHandler;
import com.bit.authservice.entity.Token;
import com.bit.authservice.exception.InvalidAuthorizationHeaderException;
import com.bit.authservice.exception.MissingAuthorizationHeaderException;
import com.bit.authservice.exception.TokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.bit.authservice.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import redis.clients.jedis.Jedis;

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

    @Mock
    private Jedis jedis;

    @InjectMocks
    private CustomLogoutHandler customLogoutHandler;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testLogout_WhenAuthorizationHeaderIsMissing_ShouldThrowMissingAuthorizationHeaderException() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(MissingAuthorizationHeaderException.class, () ->
            customLogoutHandler.logout(request, response, authentication)
        );
    }

    @Test
    public void testLogout_WhenAuthorizationHeaderIsInvalid_ShouldThrowInvalidAuthorizationHeaderException() {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        assertThrows(InvalidAuthorizationHeaderException.class, () ->
            customLogoutHandler.logout(request, response, authentication)
        );
    }

    @Test
    public void testLogout_WhenTokenNotFound_ShouldThrowTokenNotFoundException() {
        String token = "Bearer validToken";
        when(request.getHeader("Authorization")).thenReturn(token);
        when(tokenRepository.findByToken("validToken")).thenReturn(java.util.Optional.empty());

        assertThrows(TokenNotFoundException.class, () ->
            customLogoutHandler.logout(request, response, authentication)
        );
    }

//    @Test
//    public void testLogout_WhenTokenFound_ShouldMarkTokenAsLoggedOutAndSaveToRedis() {
//        String token = "Bearer validToken";
//        Token storedToken = new Token();
//        storedToken.setId(1L);
//        storedToken.setToken("validToken");
//
//        jedis = new Jedis();
//
//        when(request.getHeader("Authorization")).thenReturn(token);
//        when(tokenRepository.findByToken("validToken")).thenReturn(java.util.Optional.of(storedToken));
////        when(jedis.set(anyString(), anyString())).thenReturn(null);
//
//        customLogoutHandler.logout(request, response, authentication);
//
//        assertTrue(storedToken.isLoggedOut());
//        verify(tokenRepository, times(1)).save(storedToken);
//        verify(jedis, times(1)).set("token:1:is_logged_out", "true");
//    }

}
