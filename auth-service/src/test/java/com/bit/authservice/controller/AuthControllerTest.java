package com.bit.authservice.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthResponse;
import com.bit.authservice.dto.AuthStatus;
import com.bit.authservice.exception.InvalidRefreshTokenException;
import com.bit.authservice.exception.RedisOperationException;
import com.bit.authservice.exception.UsernameExtractionException;
import com.bit.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("testUser");
        authRequest.setPassword("testPassword");

        httpServletRequest = mock(HttpServletRequest.class);
    }

    @Test
    void testLogin_Success() throws RedisOperationException {
        // Arrange
        List<String> tokens = List.of("accessToken", "refreshToken");
        when(authService.login(authRequest)).thenReturn(tokens);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AuthStatus.LOGIN_SUCCESS, response.getBody().authStatus());
        assertEquals("accessToken", response.getBody().accessToken());
        assertEquals("refreshToken", response.getBody().refreshToken());
    }

    @Test
    void testRefreshToken_Success() throws RedisOperationException, InvalidRefreshTokenException, UsernameExtractionException {
        // Arrange
        List<String> tokens = List.of("newAccessToken", "newRefreshToken");
        when(authService.refreshToken(httpServletRequest)).thenReturn(tokens);

        // Act
        ResponseEntity<AuthResponse> response = authController.refreshToken(httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AuthStatus.TOKEN_REFRESHED_SUCCESSFULLY, response.getBody().authStatus());
        assertEquals("newAccessToken", response.getBody().accessToken());
        assertEquals("newRefreshToken", response.getBody().refreshToken());
    }
}
