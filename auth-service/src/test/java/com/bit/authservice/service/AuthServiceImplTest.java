package com.bit.authservice.service;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthUserRequest;
import com.bit.authservice.entity.AppUser;
import com.bit.authservice.entity.Role;
import com.bit.authservice.entity.Token;
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
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

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

    @Mock
    private Jedis jedis;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthRequest authRequest;
    private AppUser appUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "redisHost", "localhost");
        ReflectionTestUtils.setField(authService, "redisPort", "6379");
        ReflectionTestUtils.setField(authService, "jedis", jedis);

        authRequest = new AuthRequest("testUser", "testPass");

        appUser = AppUser.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPass")
                .roles(List.of(new Role("ROLE_USER")))
                .build();
    }

    @Test
    void givenValidRedisHostAndPort_whenInitMethodIsCalled_thenJedisInstanceIsInitialized() throws RedisOperationException {
        // Arrange
        authService = spy(authService);
        lenient().doNothing().when(authService).saveUserToken(any(), anyString());

        // Act
        authService.init();
        Jedis jedis = (Jedis) ReflectionTestUtils.getField(authService, "jedis");

        // Assert
        assertNotNull(jedis, "Jedis instance should be initialized");
    }

    @Test
    void givenInvalidPort_whenInitMethodIsCalled_thenNumberFormatExceptionIsThrown() {
        // Arrange
        ReflectionTestUtils.setField(authService, "redisPort", "invalidPort");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> authService.init(), "Invalid port should throw NumberFormatException");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAccessTokenAndRefreshToken() throws RedisOperationException {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(appUser));
        when(jwtUtils.generateAccessToken(anyString(), anyList())).thenReturn("accessToken");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("refreshToken");

        // Act
        List<String> tokens = authService.login(authRequest);

        // Assert
        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("accessToken", tokens.get(0));
        assertEquals("refreshToken", tokens.get(1));
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowAuthenticationFailedException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authService.login(authRequest));

        assertEquals("Authentication failed: Wrong username or password", exception.getMessage());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.login(authRequest));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WithValidRefreshToken_ShouldReturnNewAccessTokenAndRefreshToken() throws Exception {
        // Arrange
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtils.isValid(eq(refreshToken), any())).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(appUser));
        when(jwtUtils.generateAccessToken(anyString(), anyList())).thenReturn("newAccessToken");

        // Act
        List<String> tokens = authService.refreshToken(request);

        // Assert
        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("newAccessToken", tokens.get(0));
        assertEquals(refreshToken, tokens.get(1));
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldThrowInvalidRefreshTokenException() {
        // Arrange
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("NotBearer invalidRefreshToken");

        // Act & Assert
        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshToken(request));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        // Arrange
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.refreshToken(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WithInvalidUserDetails_ShouldThrowUserNotFoundException() {
        // Arrange
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.refreshToken(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WithUsernameExtractionFailure_ShouldThrowUsernameExtractionException() {
        // Arrange
        String refreshToken = "validRefreshToken";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(null);

        // Act & Assert
        UsernameExtractionException exception = assertThrows(UsernameExtractionException.class,
                () -> authService.refreshToken(request));

        assertEquals("Username extraction failed", exception.getMessage());
    }

    @Test
    void createUser_WithValidRequest_ShouldSaveUser() throws RoleNotFoundException {
        // Arrange
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_USER"));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(new Role("ROLE_USER")));
        when(userRepository.save(any(AppUser.class))).thenReturn(appUser);

        // Act
        authService.createUser(authUserRequest);

        // Assert
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void createUser_WithInvalidRole_ShouldThrowRoleNotFoundException() {
        // Arrange
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_INVALID"));
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class,
                () -> authService.createUser(authUserRequest));

        assertEquals("Role ROLE_INVALID not found", exception.getMessage());
    }

    @Test
    void updateUser_WithValidRequest_ShouldUpdateUser() throws RoleNotFoundException {
        // Arrange
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_USER"));
        UpdateUserMessage updateUserMessage = new UpdateUserMessage(1L, authUserRequest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(appUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(new Role("ROLE_USER")));

        // Act
        authService.updateUser(updateUserMessage);

        // Assert
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void updateUser_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        // Arrange
        AuthUserRequest authUserRequest = new AuthUserRequest("testUser", "testPass", List.of("ROLE_USER"));
        UpdateUserMessage updateUserMessage = new UpdateUserMessage(1L, authUserRequest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.updateUser(updateUserMessage));

        assertEquals("User not found with id 1", exception.getMessage());
    }

    @Test
    void restoreUser_WithValidId_ShouldRestoreUser() {
        // Arrange
        doNothing().when(userRepository).restoreUser(anyLong());

        // Act
        authService.restoreUser(1L);

        // Assert
        verify(userRepository).restoreUser(anyLong());
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteById(anyLong());

        // Act
        authService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(anyLong());
    }

    @Test
    void deleteUserPermanently_WithValidId_ShouldDeleteUserRolesAndPermanentlyDeleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteRolesForUser(anyLong());
        doNothing().when(userRepository).deletePermanently(anyLong());

        // Act
        authService.deleteUserPermanently(1L);

        // Assert
        verify(userRepository).deleteRolesForUser(anyLong());
        verify(userRepository).deletePermanently(anyLong());
    }

    @Test
    void givenJedisException_whenSaveUserTokenIsCalled_thenRedisOperationExceptionIsThrown() {
        // Arrange
        AppUser user = new AppUser();
        String jwtToken = "sample-jwt-token";
        doThrow(JedisException.class).when(jedis).set(anyString(), anyString());

        // Act & Assert
        assertThrows(RedisOperationException.class, () -> authService.saveUserToken(user, jwtToken));
        verify(tokenRepository, times(1)).save(any(Token.class));
        verify(jedis, times(1)).set(anyString(), anyString());
    }
}
