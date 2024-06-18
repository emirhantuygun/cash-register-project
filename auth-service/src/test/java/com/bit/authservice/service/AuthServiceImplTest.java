package com.bit.authservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthRequest authRequest;
    private AppUser appUser;
    private Token token;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("testUser", "testPass");

        appUser = AppUser.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPass")
                .roles(List.of(new Role("ROLE_USER")))
                .build();

        token = Token.builder()
                .id(1L)
                .token("jwtToken")
                .user(appUser)
                .loggedOut(false)
                .build();
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

        verify(tokenRepository).save(any(Token.class));
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
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);
        when(jwtUtils.isValid(refreshToken, any())).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(appUser));
        when(jwtUtils.generateAccessToken(anyString(), anyList())).thenReturn("newAccessToken");

        List<String> tokens = authService.refreshToken(request);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("newAccessToken", tokens.get(0));
        assertEquals(refreshToken, tokens.get(1));

        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldThrowInvalidRefreshTokenException() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalidRefreshToken");

        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refreshToken(request));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldThrowUserNotFoundException() throws Exception {
        String refreshToken = "validRefreshToken";
        String username = "testUser";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtUtils.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(null);
        when(jwtUtils.isValid(refreshToken, any(UserDetails.class))).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authService.refreshToken(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WithInvalidUserDetails_ShouldThrowUserNotFoundException() throws Exception {
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
    void refreshToken_WithUsernameExtractionFailure_ShouldThrowUsernameExtractionException() throws Exception {
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

//    @Test
//    void saveUserToken_WithValidToken_ShouldSaveTokenAndSetInRedis() throws RedisOperationException {
//        when(tokenRepository.save(any(Token.class))).thenReturn(token);
//
//        authService.saveUserToken(appUser, "jwtToken");
//
//        verify(tokenRepository).save(any(Token.class));
//        verify(jedis).set("token:1:is_logged_out", "false");
//        verify(jedis).set("jwtToken", "1");
//    }
//
//    @Test
//    void saveUserToken_WithRedisException_ShouldThrowRedisOperationException() {
//        when(tokenRepository.save(any(Token.class))).thenReturn(token);
//        doThrow(new JedisException("Redis error")).when(jedis).set(anyString(), anyString());
//
//        RedisOperationException exception = assertThrows(RedisOperationException.class,
//                () -> authService.saveUserToken(appUser, "jwtToken"));
//
//        assertEquals("Failed to set token status in Redis", exception.getMessage());
//    }
//
//    @Test
//    void revokeAllTokensByUser_WithValidTokens_ShouldSetTokensAsLoggedOutAndSaveInRedis() throws RedisOperationException {
//        List<Token> tokens = List.of(token);
//        when(tokenRepository.findAllTokensByUser(anyLong())).thenReturn(tokens);
//
//        authService.revokeAllTokensByUser(1L);
//
//        verify(tokenRepository).saveAll(tokens);
//        verify(jedis).set("token:1:is_logged_out", "true");
//    }
//
//    @Test
//    void revokeAllTokensByUser_WithRedisException_ShouldThrowRedisOperationException() {
//        List<Token> tokens = List.of(token);
//        when(tokenRepository.findAllTokensByUser(anyLong())).thenReturn(tokens);
//        doThrow(new JedisException("Redis error")).when(jedis).set(anyString(), anyString());
//
//        RedisOperationException exception = assertThrows(RedisOperationException.class,
//                () -> authService.revokeAllTokensByUser(1L));
//
//        assertEquals("Failed to set token status in Redis", exception.getMessage());
//    }
//
//    @Test
//    void getRolesAsString_WithValidRoles_ShouldReturnRoleNamesAsList() {
//        List<Role> roles = List.of(new Role("ROLE_USER"), new Role("ROLE_ADMIN"));
//
//        List<String> roleNames = authService.getRolesAsString(roles);
//
//        assertNotNull(roleNames);
//        assertEquals(2, roleNames.size());
//        assertTrue(roleNames.contains("ROLE_USER"));
//        assertTrue(roleNames.contains("ROLE_ADMIN"));
//    }
//
//    @Test
//    void getRolesAsRole_WithValidRoleNames_ShouldReturnRolesAsList() throws RoleNotFoundException {
//        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(new Role("ROLE_USER")));
//        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(new Role("ROLE_ADMIN")));
//
//        List<String> roleNames = List.of("ROLE_USER", "ROLE_ADMIN");
//        List<Role> roles = authService.getRolesAsRole(roleNames);
//
//        assertNotNull(roles);
//        assertEquals(2, roles.size());
//        assertTrue(roles.stream().anyMatch(role -> role.getRoleName().equals("ROLE_USER")));
//        assertTrue(roles.stream().anyMatch(role -> role.getRoleName().equals("ROLE_ADMIN")));
//    }
//
//    @Test
//    void getRolesAsRole_WithInvalidRoleName_ShouldThrowRoleNotFoundException() {
//        when(roleRepository.findByRoleName("ROLE_INVALID")).thenReturn(Optional.empty());
//
//        List<String> roleNames = List.of("ROLE_INVALID");
//
//        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class,
//                () -> authService.getRolesAsRole(roleNames));
//
//        assertEquals("Role ROLE_INVALID not found", exception.getMessage());
//    }
}
