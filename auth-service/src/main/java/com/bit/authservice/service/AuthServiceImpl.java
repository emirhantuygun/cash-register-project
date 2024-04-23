package com.bit.authservice.service;

import com.bit.authservice.AuthServiceApplication;
import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.UserRequest;
import com.bit.authservice.entity.AppUser;
import com.bit.authservice.entity.Role;
import com.bit.authservice.entity.Token;
import com.bit.authservice.repository.RoleRepository;
import com.bit.authservice.repository.TokenRepository;
import com.bit.authservice.repository.UserRepository;
import com.bit.authservice.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private static final Logger logger = LogManager.getLogger(AuthServiceApplication.class);

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

                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

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
    public void createUser(UserRequest userRequest) {

        var encodedPassword = passwordEncoder.encode(userRequest.getPassword());

        var appUser = AppUser.builder()
                .username(userRequest.getUsername())
                .password(encodedPassword)
                .roles(getRolesAsRole(userRequest.getRoles()))
                .build();

        userRepository.save(appUser);
    }

    @Override
    public void updateUser(Long id, UserRequest userRequest) {
        AppUser existingUser = userRepository.findById(id).orElseThrow();
        var encodedPassword = passwordEncoder.encode(userRequest.getPassword());

        existingUser.setUsername(userRequest.getUsername());
        existingUser.setPassword(encodedPassword);
        existingUser.setRoles(getRolesAsRole(userRequest.getRoles()));

        userRepository.save(existingUser);
    }

    @Override
    public void restoreUser(Long id) {
        userRepository.restoreUser(id);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
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
    }
    private void revokeAllTokenByUser(long id) {
        List<Token> validTokens = tokenRepository.findAllTokensByUser(id);

        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t -> t.setLoggedOut(true));

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
