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
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final TokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;
    private Jedis jedis;

    @PostConstruct
    public void init() {
        log.trace("Entering init method in AuthServiceImpl");
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jedis.flushAll();        // Remove all keys from Redis
            jedis.close();           // Close the Redis connection
        }));
        log.trace("Exiting init method in AuthServiceImpl");
    }

    @Override
    public List<String> login(AuthRequest authRequest) throws RedisOperationException {
        log.trace("Entering login method in AuthServiceImpl");
        try {
            var authToken = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
            authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            log.error("Authentication failed: Wrong username or password");
            throw new AuthenticationFailedException("Authentication failed: Wrong username or password");
        }

        AppUser appUser = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found");
                    return new UserNotFoundException("User not found");
                });

        String accessToken = jwtUtils.generateAccessToken(authRequest.getUsername(), getRolesAsString(appUser.getRoles()));
        String refreshToken = jwtUtils.generateRefreshToken(authRequest.getUsername());

        revokeAllTokensByUser(appUser.getId());
        saveUserToken(appUser, accessToken);

        log.trace("Exiting login method in AuthServiceImpl");
        return Arrays.asList(accessToken, refreshToken);
    }

    @Override
    public List<String> refreshToken(HttpServletRequest request) throws InvalidRefreshTokenException, UsernameExtractionException, UserNotFoundException, RedisOperationException {
        log.trace("Entering refreshToken method in AuthServiceImpl");
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String username = jwtUtils.extractUsername(refreshToken);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null && jwtUtils.isValid(refreshToken, userDetails)) {
                    AppUser appUser = userRepository.findByUsername(username)
                            .orElseThrow(() -> {
                                log.error("User not found");
                                return new UserNotFoundException("User not found");
                            });

                    String accessToken = jwtUtils.generateAccessToken(username, getRolesAsString(appUser.getRoles()));

                    revokeAllTokensByUser(appUser.getId());
                    saveUserToken(appUser, accessToken);

                    log.trace("Exiting refreshToken method in AuthServiceImpl");
                    return Arrays.asList(accessToken, refreshToken);
                } else {
                    log.error("User not found");
                    throw new UserNotFoundException("User not found");
                }
            } else {
                log.error("Username extraction failed");
                throw new UsernameExtractionException("Username extraction failed");
            }
        } else {
            log.error("Invalid refresh token");
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.create}")
    public void createUser(AuthUserRequest authUserRequest) throws RoleNotFoundException {
        log.trace("Entering createUser method in AuthServiceImpl");
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        var appUser = AppUser.builder()
                .username(authUserRequest.getUsername())
                .password(encodedPassword)
                .roles(getRolesAsRole(authUserRequest.getRoles()))
                .build();

        userRepository.save(appUser);
        log.trace("Exiting createUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.update}")
    public void updateUser(UpdateUserMessage updateUserMessage) throws RoleNotFoundException {
        log.trace("Entering updateUser method in AuthServiceImpl");
        Long id = updateUserMessage.getId();
        AuthUserRequest authUserRequest = updateUserMessage.getAuthUserRequest();

        AppUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id " + id);
                    return new UserNotFoundException("User not found with id " + id);
                });
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        existingUser.setUsername(authUserRequest.getUsername());
        existingUser.setPassword(encodedPassword);
        existingUser.setRoles(getRolesAsRole(authUserRequest.getRoles()));

        userRepository.save(existingUser);
        log.trace("Exiting updateUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.restore}")
    public void restoreUser(Long id) {
        log.trace("Entering restoreUser method in AuthServiceImpl");
        userRepository.restoreUser(id);
        log.trace("Exiting restoreUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.delete}")
    public void deleteUser(Long id) {
        log.trace("Entering deleteUser method in AuthServiceImpl");
        userRepository.deleteById(id);
        log.trace("Exiting deleteUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.deletePermanent}")
    public void deleteUserPermanently(Long id) {
        log.trace("Entering deleteUserPermanently method in AuthServiceImpl");

        userRepository.deleteRolesForUser(id);
        userRepository.deletePermanently(id);

        log.trace("Exiting deleteUserPermanently method in AuthServiceImpl");
    }

    protected void saveUserToken(AppUser user, String jwtToken) throws RedisOperationException {
        log.trace("Entering saveUserToken method in AuthServiceImpl");
        var token = Token.builder()
                .token(jwtToken)
                .user(user)
                .loggedOut(false)
                .build();
        tokenRepository.save(token);

        try {
            jedis.set("token:" + token.getId() + ":is_logged_out", "false");
            jedis.set(jwtToken, String.valueOf(token.getId()));
        } catch (JedisException e) {
            log.error("Failed to set token status in Redis: {}", e.getMessage());
            throw new RedisOperationException("Failed to set token status in Redis", e);
        }
        log.trace("Exiting saveUserToken method in AuthServiceImpl");
    }

    private void revokeAllTokensByUser(long id) throws RedisOperationException {
        log.trace("Entering revokeAllTokensByUser method in AuthServiceImpl");
        List<Token> validTokens = tokenRepository.findAllTokensByUser(id);
        if (validTokens.isEmpty()) {
            log.debug("No valid token found");
            return;
        }

        for (Token token : validTokens) {
            token.setLoggedOut(true);
            try {
                jedis.set("token:" + token.getId() + ":is_logged_out", "true");
            } catch (JedisException e) {
                log.error("Failed to set token status in Redis: {}", e.getMessage());
                throw new RedisOperationException("Failed to set token status in Redis", e);
            }
        }

        tokenRepository.saveAll(validTokens);
        log.trace("Exiting revokeAllTokensByUser method in AuthServiceImpl");
    }

    private List<String> getRolesAsString(List<Role> roles) {
        log.trace("Entering getRolesAsString method in AuthServiceImpl");
        try {
            return roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
        } finally {
            log.trace("Exiting getRolesAsString method in AuthServiceImpl");
        }
    }

    private List<Role> getRolesAsRole(List<String> roles) throws RoleNotFoundException {
        log.trace("Entering getRolesAsRole method in AuthServiceImpl");

        List<Role> rolesList = new ArrayList<>();
        for (String roleName : roles) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> {
                        log.error("Role " + roleName + " not found");
                        return new RoleNotFoundException("Role " + roleName + " not found");
                    });
            rolesList.add(role);
        }

        log.trace("Exiting getRolesAsRole method in AuthServiceImpl");
        return rolesList;
    }
}
