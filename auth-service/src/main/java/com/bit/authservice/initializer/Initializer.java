package com.bit.authservice.initializer;

import com.bit.authservice.entity.AppUser;
import com.bit.authservice.entity.Role;
import com.bit.authservice.repository.RoleRepository;
import com.bit.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {


    @Value("#{'${default-roles}'.split(', ')}")
    private final List<String> DEFAULT_ROLES;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        for (String roleName : DEFAULT_ROLES) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    private void initializeAdminUser() {

        var encodedPassword = passwordEncoder.encode("admin");
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();

        userRepository.save(AppUser.builder()
                .username("admin")
                .password(encodedPassword)
                .roles(List.of(adminRole)).build());
    }
}
