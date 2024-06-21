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
        log.info("Entering init method in AuthServiceImpl");
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jedis.flushAll();        // Remove all keys from Redis
            jedis.close();           // Close the Redis connection
        }));
        log.info("Exiting init method in AuthServiceImpl");
    }

    @Override
    public List<String> login(AuthRequest authRequest) throws RedisOperationException {
        log.info("Entering login method in AuthServiceImpl");
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

        log.info("Exiting login method in AuthServiceImpl");
        return Arrays.asList(accessToken, refreshToken);
    }

    @Override
    public List<String> refreshToken(HttpServletRequest request) throws InvalidRefreshTokenException, UsernameExtractionException, UserNotFoundException, RedisOperationException {
        log.info("Entering refreshToken method in AuthServiceImpl");
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

                    log.info("Exiting refreshToken method in AuthServiceImpl");
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
        log.info("Entering createUser method in AuthServiceImpl");
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        var appUser = AppUser.builder()
                .username(authUserRequest.getUsername())
                .password(encodedPassword)
                .roles(getRolesAsRole(authUserRequest.getRoles()))
                .build();

        userRepository.save(appUser);
        log.info("Exiting createUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.update}")
    public void updateUser(UpdateUserMessage updateUserMessage) throws RoleNotFoundException {
        log.info("Entering updateUser method in AuthServiceImpl");
        Long id = updateUserMessage.getId();
        AuthUserRequest authUserRequest = updateUserMessage.getAuthUserRequest();

        AppUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        existingUser.setUsername(authUserRequest.getUsername());
        existingUser.setPassword(encodedPassword);
        existingUser.setRoles(getRolesAsRole(authUserRequest.getRoles()));

        userRepository.save(existingUser);
        log.info("Exiting updateUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.restore}")
    public void restoreUser(Long id) {
        log.info("Entering restoreUser method in AuthServiceImpl");
        userRepository.restoreUser(id);
        log.info("Exiting restoreUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.delete}")
    public void deleteUser(Long id) {
        log.info("Entering deleteUser method in AuthServiceImpl");
        userRepository.deleteById(id);
        log.info("Exiting deleteUser method in AuthServiceImpl");
    }

    @RabbitListener(queues = "${rabbitmq.queue.deletePermanent}")
    public void deleteUserPermanently(Long id) {
        log.info("Entering deleteUserPermanently method in AuthServiceImpl");
        userRepository.deleteRolesForUser(id);
        userRepository.deletePermanently(id);
        log.info("Exiting deleteUserPermanently method in AuthServiceImpl");
    }

    protected void saveUserToken(AppUser user, String jwtToken) throws RedisOperationException {
        log.debug("Saving user token in Redis");
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
    }

    private void revokeAllTokensByUser(long id) throws RedisOperationException {
        log.debug("Revoking all tokens for user");
        List<Token> validTokens = tokenRepository.findAllTokensByUser(id);
        if (validTokens.isEmpty()) {
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
    }

    private List<String> getRolesAsString(List<Role> roles) {
        log.debug("Getting roles as string");
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    private List<Role> getRolesAsRole(List<String> roles) throws RoleNotFoundException {
        log.debug("Getting roles as role entity");
        List<Role> rolesList = new ArrayList<>();
        for (String roleName : roles) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RoleNotFoundException("Role " + roleName + " not found"));
            rolesList.add(role);
        }
        return rolesList;
    }

}
