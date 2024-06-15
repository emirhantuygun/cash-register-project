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
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jedis.flushAll();        // Remove all keys from Redis
            jedis.close();           // Close the Redis connection
        }));
    }

    @Override
    public List<String> login(AuthRequest authRequest) throws RedisOperationException {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
            authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            throw new AuthenticationFailedException("Authentication failed: Wrong username or password");
        }

        AppUser appUser = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String accessToken = jwtUtils.generateAccessToken(authRequest.getUsername(), getRolesAsString(appUser.getRoles()));
        String refreshToken = jwtUtils.generateRefreshToken(authRequest.getUsername());

        revokeAllTokensByUser(appUser.getId());
        saveUserToken(appUser, accessToken);

        return Arrays.asList(accessToken, refreshToken);
    }

    @Override
    public List<String> refreshToken(HttpServletRequest request) throws InvalidRefreshTokenException, UsernameExtractionException, UserNotFoundException, RedisOperationException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String username = jwtUtils.extractUsername(refreshToken);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null && jwtUtils.isValid(refreshToken, userDetails)) {
                    AppUser appUser = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UserNotFoundException("User not found"));

                    String accessToken = jwtUtils.generateAccessToken(username, getRolesAsString(appUser.getRoles()));

                    revokeAllTokensByUser(appUser.getId());
                    saveUserToken(appUser, accessToken);

                    return Arrays.asList(accessToken, refreshToken);
                } else {
                    throw new UserNotFoundException("User not found");
                }
            } else {
                throw new UsernameExtractionException("Username extraction failed");
            }
        } else {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.create}")
    public void createUser(AuthUserRequest authUserRequest) throws RoleNotFoundException {

        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        var appUser = AppUser.builder()
                .username(authUserRequest.getUsername())
                .password(encodedPassword)
                .roles(getRolesAsRole(authUserRequest.getRoles()))
                .build();

        userRepository.save(appUser);
    }


    @RabbitListener(queues = "${rabbitmq.queue.update}")
    public void updateUserWrapped(UpdateUserMessage updateUserMessage) throws RoleNotFoundException {

        Long id = updateUserMessage.getId();
        AuthUserRequest authUserRequest = updateUserMessage.getAuthUserRequest();

        AppUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        existingUser.setUsername(authUserRequest.getUsername());
        existingUser.setPassword(encodedPassword);
        existingUser.setRoles(getRolesAsRole(authUserRequest.getRoles()));

        userRepository.save(existingUser);
    }

    @RabbitListener(queues = "${rabbitmq.queue.restore}")
    public void restoreUser(Long id) {
        userRepository.restoreUser(id);
    }

    @RabbitListener(queues = "${rabbitmq.queue.delete}")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @RabbitListener(queues = "${rabbitmq.queue.deletePermanent}")
    public void deleteUserPermanently(Long id) {
        userRepository.deleteRolesForUser(id);
        userRepository.deletePermanently(id);
    }

    private void saveUserToken(AppUser user, String jwtToken) throws RedisOperationException {
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
            throw new RedisOperationException("Failed to set token status in Redis", e);
        }
    }

    private void revokeAllTokensByUser(long id) throws RedisOperationException {
        List<Token> validTokens = tokenRepository.findAllTokensByUser(id);
        if (validTokens.isEmpty()) {
            return;
        }

        for (Token token : validTokens) {
            token.setLoggedOut(true);
            try {
                jedis.set("token:" + token.getId() + ":is_logged_out", "true");
            } catch (JedisException e) {
                throw new RedisOperationException("Failed to set token status in Redis", e);
            }
        }

        tokenRepository.saveAll(validTokens);
    }

    private List<String> getRolesAsString(List<Role> roles) {
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    private List<Role> getRolesAsRole(List<String> roles) throws RoleNotFoundException {
        List<Role> rolesList = new ArrayList<>();
        for (String roleName : roles) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RoleNotFoundException("Role " + roleName + " not found"));
            rolesList.add(role);
        }
        return rolesList;
    }

}
