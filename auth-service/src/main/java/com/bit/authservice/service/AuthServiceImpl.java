package com.bit.authservice.service;

import com.bit.authservice.AuthServiceApplication;
import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthUserRequest;
import com.bit.authservice.entity.AppUser;
import com.bit.authservice.entity.Role;
import com.bit.authservice.entity.Token;
import com.bit.authservice.repository.RoleRepository;
import com.bit.authservice.repository.TokenRepository;
import com.bit.authservice.repository.UserRepository;
import com.bit.authservice.util.JwtUtils;
import com.bit.authservice.wrapper.UpdateUserMessage;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final TokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LogManager.getLogger(AuthServiceApplication.class);
    private Jedis jedis;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    @PostConstruct
    public void init() {
        this.jedis = new Jedis(redisHost, Integer.parseInt(redisPort));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Remove all keys from Redis
            jedis.flushAll();
            // Close the Redis connection
            jedis.close();
        }));
    }

    @Override
    public List<String> login(AuthRequest authRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());

        authenticationManager.authenticate(authToken);

        AppUser appUser = userRepository.findByUsername(authRequest.getUsername()).orElseThrow();
        String accessToken = jwtUtils.generateAccessToken(authRequest.getUsername(), getRolesAsString(appUser.getRoles()));
        String refreshToken = jwtUtils.generateRefreshToken(authRequest.getUsername());

        revokeAllTokenByUser(appUser.getId());
        saveUserToken(appUser, accessToken);

        return Arrays.asList(accessToken, refreshToken);
    }

    @Override
    public List<String> refreshToken(HttpServletRequest request) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            String username = jwtUtils.extractUsername(refreshToken);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null && jwtUtils.isValid(refreshToken, userDetails)) {

                    var appUser = userRepository.findByUsername(username).orElseThrow();
                    var accessToken = jwtUtils.generateAccessToken(username, getRolesAsString(appUser.getRoles()));

                    revokeAllTokenByUser(appUser.getId());
                    saveUserToken(appUser, accessToken);

                    return Arrays.asList(accessToken, refreshToken);
                }
                throw new RuntimeException("User not found or invalid token!");
            }
        }
        throw new RuntimeException("Invalid refresh token!");
    }

    @Override
    @RabbitListener(queues = "${rabbitmq.queue.create}")
    public void createUser(AuthUserRequest authUserRequest) {

        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        var appUser = AppUser.builder()
                .username(authUserRequest.getUsername())
                .password(encodedPassword)
                .roles(getRolesAsRole(authUserRequest.getRoles()))
                .build();

        userRepository.save(appUser);
    }

    @Override
    public void updateUser(Long id, AuthUserRequest authUserRequest) {
        AppUser existingUser = userRepository.findById(id).orElseThrow();
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        existingUser.setUsername(authUserRequest.getUsername());
        existingUser.setPassword(encodedPassword);
        existingUser.setRoles(getRolesAsRole(authUserRequest.getRoles()));

        userRepository.save(existingUser);
    }

    @Override
    @RabbitListener(queues = "${rabbitmq.queue.update}")
    public void updateUserWrapped(UpdateUserMessage updateUserMessage) {

        Long id = updateUserMessage.getId();
        AuthUserRequest authUserRequest = updateUserMessage.getAuthUserRequest();

        AppUser existingUser = userRepository.findById(id).orElseThrow();
        var encodedPassword = passwordEncoder.encode(authUserRequest.getPassword());

        existingUser.setUsername(authUserRequest.getUsername());
        existingUser.setPassword(encodedPassword);
        existingUser.setRoles(getRolesAsRole(authUserRequest.getRoles()));

        userRepository.save(existingUser);
    }

    @Override
    @RabbitListener(queues = "${rabbitmq.queue.restore}")
    public void restoreUser(Long id) {
        userRepository.restoreUser(id);
    }

    @Override
    @RabbitListener(queues = "${rabbitmq.queue.delete}")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @RabbitListener(queues = "${rabbitmq.queue.deletePermanent}")
    public void deleteUserPermanently(Long id) {
        userRepository.deleteRolesForUser(id);
        userRepository.deletePermanently(id);
    }

    private void saveUserToken(AppUser user, String jwtToken) {

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
            throw new RuntimeException("Failed to set token status in Redis", e);
        }
    }

    private void revokeAllTokenByUser(long id) {
        List<Token> validTokens = tokenRepository.findAllTokensByUser(id);

        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t -> {
            t.setLoggedOut(true);
            try {
                jedis.set("token:" + t.getId() + ":is_logged_out", "true");

            } catch (JedisException e) {
                throw new RuntimeException("Failed to set token status in Redis", e);
            }
        });

        tokenRepository.saveAll(validTokens);
    }

    private List<String> getRolesAsString(List<Role> roles) {
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    private List<Role> getRolesAsRole(List<String> roles) {
        List<Role> rolesList = new ArrayList<>();
        roles.forEach(roleName -> {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> {
                        logger.error("Role {} not found", roleName);
                        return new RuntimeException("Role " + roleName + " not found");
                    });
            rolesList.add(role);
        });
        return rolesList;
    }

}
