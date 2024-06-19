package com.bit.authservice.config;

import com.bit.authservice.exception.InvalidAuthorizationHeaderException;
import com.bit.authservice.exception.MissingAuthorizationHeaderException;
import com.bit.authservice.exception.TokenNotFoundException;
import com.bit.authservice.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
