package com.bit.authservice.config;

import com.bit.authservice.repository.UserRepository;
import com.bit.authservice.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This class is responsible for configuring authentication and authorization in the application.
 * It sets up the security filter chain, user details service, authentication manager, authentication provider,
 * and password encoder.
 * @author Emirhan Tuygun
 */
@Configuration
@RequiredArgsConstructor
public class AuthenticationConfig {

    private final UserRepository userRepository;
    private final CustomLogoutHandler logoutHandler;

    /**
     * This method configures the security filter chain for the application.
     * It disables CSRF protection, sets up URL-based authorization, configures logout functionality,
     * and sets the session management policy to stateless.
     *
     * @param http The HttpSecurity object used to configure security settings.
     * @return A SecurityFilterChain object representing the configured security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requestMatcher -> requestMatcher
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(l -> l
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()
                        ))
                .sessionManagement(
                        sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    /**
     * This method creates and returns an instance of UserDetailsService.
     * The UserDetailsServiceImpl class is responsible for loading user-specific data from the database.
     *
     * @return An instance of UserDetailsService, which is used by the authentication provider to authenticate users.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(userRepository);
    }

    /**
     * This method creates and returns an instance of AuthenticationManager.
     * The AuthenticationManager is responsible for managing authentication processes in Spring Security.
     * It is used by the authentication provider to authenticate users.
     *
     * @param config The AuthenticationConfiguration object provides access to the authentication manager.
     * @return An instance of AuthenticationManager, which is used by the authentication provider to authenticate users.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * This method creates and returns an instance of AuthenticationProvider.
     * The AuthenticationProvider is responsible for authenticating users in the application.
     * It uses the DaoAuthenticationProvider implementation, which retrieves user-specific data from the UserDetailsService
     * and compares the provided password with the stored hashed password using the PasswordEncoder.
     *
     * @return An instance of AuthenticationProvider, which is used by the authentication manager to authenticate users.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());

        return authenticationProvider;
    }

    /**
     * This method creates and returns an instance of PasswordEncoder.
     * The PasswordEncoder is responsible for encoding passwords before storing them in the database.
     * In this case, we are using BCryptPasswordEncoder, which is a widely used and secure password hashing algorithm.
     *
     * @return An instance of PasswordEncoder, which is used by the authentication provider to hash passwords.
     */
    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
