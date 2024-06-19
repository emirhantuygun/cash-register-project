package com.bit.authservice.service;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthUserRequest;
import com.bit.authservice.entity.AppUser;
import com.bit.authservice.entity.Role;
import com.bit.authservice.exception.*;
import com.bit.authservice.repository.RoleRepository;
import com.bit.authservice.repository.TokenRepository;
import com.bit.authservice.repository.UserRepository;
import com.bit.authservice.util.JwtUtils;
import com.bit.authservice.wrapper.UpdateUserMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthRequest authRequest;
    private AppUser appUser;

    @BeforeEach
    void setUp() throws RedisOperationException {
        authRequest = new AuthRequest("testUser", "testPass");

        appUser = AppUser.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPass")
                .roles(List.of(new Role("ROLE_USER")))
                .build();

        authService = spy(authService);
        lenient().doNothing().when(authService).saveUserToken(any(), anyString());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAccessTokenAndRefreshToken() throws RedisOperationException {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(appUser));
        when(jwtUtils.generateAccessToken(anyString(), anyList())).thenReturn("accessToken");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("refreshToken");

        List<String> tokens = authService.login(authRequest);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("accessToken", tokens.get(0));
        assertEquals("refreshToken", tokens.get(1));
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowAuthenticationFailedException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authService.login(authRequest));

        assertEquals("Authentication failed: Wrong username or password", exception.getMessage());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.login(authRequest));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WithValidRefreshToken_ShouldReturnNewAccessTokenAndRefreshToken() throws Exception {
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtils.isValid(eq(refreshToken), any())).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(appUser));
        when(jwtUtils.generateAccessToken(anyString(), anyList())).thenReturn("newAccessToken");

        List<String> tokens = authService.refreshToken(request);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("newAccessToken", tokens.get(0));
        assertEquals(refreshToken, tokens.get(1));
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldThrowInvalidRefreshTokenException() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("NotBearer invalidRefreshToken");

        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshToken(request));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.refreshToken(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WithInvalidUserDetails_ShouldThrowUserNotFoundException() {
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.refreshToken(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WithUsernameExtractionFailure_ShouldThrowUsernameExtractionException() {
        String refreshToken = "validRefreshToken";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(null);

        UsernameExtractionException exception = assertThrows(UsernameExtractionException.class,
                () -> authService.refreshToken(request));

        assertEquals("Username extraction failed", exception.getMessage());
    }

    // Additional tests for RabbitMQ listeners can be added here if needed

    @Test
    void createUser_WithValidRequest_ShouldSaveUser() throws RoleNotFoundException {
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_USER"));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(new Role("ROLE_USER")));
        when(userRepository.save(any(AppUser.class))).thenReturn(appUser);

        authService.createUser(authUserRequest);

        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void createUser_WithInvalidRole_ShouldThrowRoleNotFoundException() {
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_INVALID"));
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());

        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class,
                () -> authService.createUser(authUserRequest));

        assertEquals("Role ROLE_INVALID not found", exception.getMessage());
    }

    @Test
    void updateUser_WithValidRequest_ShouldUpdateUser() throws RoleNotFoundException {
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_USER"));
        UpdateUserMessage updateUserMessage = new UpdateUserMessage(1L, authUserRequest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(appUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(new Role("ROLE_USER")));

        authService.updateUserWrapped(updateUserMessage);

        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void updateUser_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_USER"));
        UpdateUserMessage updateUserMessage = new UpdateUserMessage(1L, authUserRequest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.updateUserWrapped(updateUserMessage));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void restoreUser_WithValidId_ShouldRestoreUser() {
        doNothing().when(userRepository).restoreUser(anyLong());

        authService.restoreUser(1L);

        verify(userRepository).restoreUser(anyLong());
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        doNothing().when(userRepository).deleteById(anyLong());

        authService.deleteUser(1L);

        verify(userRepository).deleteById(anyLong());
    }

    @Test
    void deleteUserPermanently_WithValidId_ShouldDeleteUserRolesAndPermanentlyDeleteUser() {
        doNothing().when(userRepository).deleteRolesForUser(anyLong());
        doNothing().when(userRepository).deletePermanently(anyLong());

        authService.deleteUserPermanently(1L);

        verify(userRepository).deleteRolesForUser(anyLong());
        verify(userRepository).deletePermanently(anyLong());
    }

}
