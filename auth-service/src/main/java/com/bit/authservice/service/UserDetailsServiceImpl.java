package com.bit.authservice.service;

import com.bit.authservice.entity.AppUser;
import com.bit.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class implements the UserDetailsService interface provided by Spring Security.
 * It is responsible for loading user-specific data from the database and providing it to the Spring Security framework.
 * @author Emirhan Tuygun
 */
@Log4j2
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * This method is responsible for loading user-specific data from the database based on the provided username.
     * It is called by the Spring Security framework during the authentication process.
     *
     * @param username The username of the user to be loaded.
     * @return A UserDetails object containing the loaded user's information and authorities.
     * @throws UsernameNotFoundException If the user with the given username is not found in the database.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.trace("Entering loadUserByUsername method in UserDetailsServiceImpl with username: {}", username);

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String roleName = "ROLE_" + role.getRoleName();
                    log.debug("Mapping role: {} to GrantedAuthority: {}", role.getRoleName(), roleName);
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toSet());

        log.trace("Exiting loadUserByUsername method in UserDetailsServiceImpl with username: {}", username);
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
