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

/**
 * Implementation of the AuthService interface.
 * Manages user authentication, token generation, and user data manipulation.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    private final static String NOT_FOUND_ERROR_MESSAGE = "User not found";

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final TokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;
    private Jedis jedis;

    /**
     * Initializes the Jedis instance for Redis operations.
     * This method is called after the bean is created and all properties are set.
     * It sets up a shutdown hook to flush all keys from Redis and close the connection when the JVM exits.
     */
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

        // Authenticating the credentials
        try {
            var authToken = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
            authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            log.error("Authentication failed: Wrong username or password");
            throw new AuthenticationFailedException("Authentication failed: Wrong username or password");
        }
        log.info("Authentication successful");

        // Finding the user from database
        AppUser appUser = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> {
                    log.error(NOT_FOUND_ERROR_MESSAGE);
                    return new UserNotFoundException(NOT_FOUND_ERROR_MESSAGE);
                });

        // Generating access and refresh tokens
        String accessToken = jwtUtils.generateAccessToken(authRequest.getUsername(), getRolesAsString(appUser.getRoles()));
        String refreshToken = jwtUtils.generateRefreshToken(authRequest.getUsername());

        // Revoking other tokens of the same user
        revokeAllTokensByUser(appUser.getId());
        saveUserToken(appUser, accessToken);

        log.trace("Exiting login method in AuthServiceImpl");
        return Arrays.asList(accessToken, refreshToken);
    }

    @Override
    public List<String> refreshToken(HttpServletRequest request) throws InvalidRefreshTokenException, UsernameExtractionException, UserNotFoundException, RedisOperationException {
        log.trace("Entering refreshToken method in AuthServiceImpl");
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Getting token from the authentication header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String username = jwtUtils.extractUsername(refreshToken);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validating the refresh token
                if (userDetails != null && jwtUtils.isValid(refreshToken, userDetails)) {

                    log.info("Refresh token is valid");
                    AppUser appUser = userRepository.findByUsername(username)
                            .orElseThrow(() -> {
                                log.error(NOT_FOUND_ERROR_MESSAGE);
                                return new UserNotFoundException(NOT_FOUND_ERROR_MESSAGE);
                            });

                    // Generating the access token
                    String accessToken = jwtUtils.generateAccessToken(username, getRolesAsString(appUser.getRoles()));

                    revokeAllTokensByUser(appUser.getId());
                    saveUserToken(appUser, accessToken);

                    log.trace("Exiting refreshToken method in AuthServiceImpl");
                    return Arrays.asList(accessToken, refreshToken);
                } else {
                    log.error(NOT_FOUND_ERROR_MESSAGE);
                    throw new UserNotFoundException(NOT_FOUND_ERROR_MESSAGE);
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

    /**
     * Listens to the RabbitMQ queue for creating new user requests.
     * When a new user request is received, it creates a new user in the database.
     *
     * @param authUserRequest The request containing the username, password, and roles for the new user.
     * @throws RoleNotFoundException If any of the roles specified in the request are not found in the database.
     */
    @RabbitListener(queues = "${rabbitmq.queue.create}")
    public void createUser(AuthUserRequest authUserRequest) throws RoleNotFoundException {
        log.trace("Entering createUser method in AuthServiceImpl");

        // Password encryption
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        // Creating the user object
        var appUser = AppUser.builder()
                .username(authUserRequest.getUsername())
                .password(encodedPassword)
                .roles(getRolesAsRole(authUserRequest.getRoles()))
                .build();

        userRepository.save(appUser);
        log.info("User created in Auth Service asynchronously");

        log.trace("Exiting createUser method in AuthServiceImpl");
    }

    /**
     * Listens to the RabbitMQ queue for updating user requests.
     * When an update user request is received, it updates the user in the database.
     *
     * @param updateUserMessage The message containing the user ID and the updated user request.
     * @throws RoleNotFoundException If any of the roles specified in the request are not found in the database.
     */
    @RabbitListener(queues = "${rabbitmq.queue.update}")
    public void updateUser(UpdateUserMessage updateUserMessage) throws RoleNotFoundException {
        log.trace("Entering updateUser method in AuthServiceImpl");

        // Getting id and AuthUserRequest from updateUserMessage
        Long id = updateUserMessage.getId();
        AuthUserRequest authUserRequest = updateUserMessage.getAuthUserRequest();
        log.debug("User is updating with id: {}", id);

        // Getting the existing user object
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
        log.info("User updated in Auth Service asynchronously");

        log.trace("Exiting updateUser method in AuthServiceImpl");
    }

    /**
     * Listens to the RabbitMQ queue for restoring user requests.
     * When a restore user request is received, it restores the user in the database.
     *
     * @param id The unique identifier of the user to be restored.
     */
    @RabbitListener(queues = "${rabbitmq.queue.restore}")
    public void restoreUser(Long id) {
        log.trace("Entering restoreUser method in AuthServiceImpl");

        userRepository.restoreUser(id);
        log.info("User restored in Auth Service asynchronously");

        log.trace("Exiting restoreUser method in AuthServiceImpl");
    }

    /**
     * Listens to the RabbitMQ queue for deleting user requests.
     * When a delete user request is received, it deletes the user in the database.
     *
     * @param id The unique identifier of the user to be deleted.
     */
    @RabbitListener(queues = "${rabbitmq.queue.delete}")
    public void deleteUser(Long id) {
        log.trace("Entering deleteUser method in AuthServiceImpl");

        userRepository.deleteById(id);
        log.info("User soft-deleted in Auth Service asynchronously");

        log.trace("Exiting deleteUser method in AuthServiceImpl");
    }

    /**
     * Listens to the RabbitMQ queue for permanently deleting user requests.
     * When a delete user permanently request is received, it deletes the user and their associated roles in the database.
     *
     * @param id The unique identifier of the user to be permanently deleted.
     */
    @RabbitListener(queues = "${rabbitmq.queue.deletePermanent}")
    public void deleteUserPermanently(Long id) {
        log.trace("Entering deleteUserPermanently method in AuthServiceImpl");

        userRepository.deleteRolesForUser(id);
        userRepository.deletePermanently(id);
        log.info("User permanently deleted in Auth Service asynchronously");

        log.trace("Exiting deleteUserPermanently method in AuthServiceImpl");
    }

    /**
     * Saves the user's JWT token in the database and Redis.
     *
     * @param user     The user associated with the token.
     * @param jwtToken The JWT token to be saved.
     * @throws RedisOperationException If there is an error while saving the token status in Redis.
     */
    protected void saveUserToken(AppUser user, String jwtToken) throws RedisOperationException {
        log.trace("Entering saveUserToken method in AuthServiceImpl");

        // Creating a token object
        var token = Token.builder()
                .token(jwtToken)
                .user(user)
                .loggedOut(false)
                .build();
        tokenRepository.save(token);

        // Storing logout status of the token
        try {
            jedis.set("token:" + token.getId() + ":is_logged_out", "false");
            jedis.set(jwtToken, String.valueOf(token.getId()));
        } catch (JedisException e) {
            log.error("Failed to set token status in Redis: {}", e.getMessage());
            throw new RedisOperationException("Failed to set token status in Redis", e);
        }
        log.info("Token is saved");

        log.trace("Exiting saveUserToken method in AuthServiceImpl");
    }

    /**
     * Revokes all tokens associated with a specific user.
     * This method sets the 'loggedOut' flag to true for all valid tokens of the user in the database.
     * It also updates the corresponding Redis key to reflect the logged out status.
     *
     * @param id The unique identifier of the user whose tokens need to be revoked.
     * @throws RedisOperationException If there is an error while saving the token status in Redis.
     */
    private void revokeAllTokensByUser(long id) throws RedisOperationException {
        log.trace("Entering revokeAllTokensByUser method in AuthServiceImpl");

        // Getting valid tokens of the user
        List<Token> validTokens = tokenRepository.findAllTokensByUser(id);
        if (validTokens.isEmpty()) {
            log.debug("No valid token found");
            return;
        }

        // Setting logout status as true for all the valid tokens
        for (Token token : validTokens) {
            token.setLoggedOut(true);
            try {
                jedis.set("token:" + token.getId() + ":is_logged_out", "true");
            } catch (JedisException e) {
                log.error("Failed to set token status in Redis: {}", e.getMessage());
                throw new RedisOperationException("Failed to set token status in Redis", e);
            }
        }
        log.info("Revoked other tokens of the user with id {}", id);

        tokenRepository.saveAll(validTokens);
        log.trace("Exiting revokeAllTokensByUser method in AuthServiceImpl");
    }

    /**
     * Converts a list of Role objects to a list of role names.
     *
     * @param roles The list of Role objects to be converted.
     * @return A list of role names extracted from the input list of Role objects.
     */
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

    /**
     * Converts a list of role names to a list of Role objects.
     *
     * @param roles The list of role names to be converted.
     * @return A list of Role objects corresponding to the input list of role names.
     * @throws RoleNotFoundException If any of the roles specified in the input list are not found in the database.
     */
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
