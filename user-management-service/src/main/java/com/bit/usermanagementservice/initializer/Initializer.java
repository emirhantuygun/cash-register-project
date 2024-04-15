package com.bit.usermanagementservice.initializer;

import com.bit.usermanagementservice.entity.AppUser;
import com.bit.usermanagementservice.entity.Role;
import com.bit.usermanagementservice.repository.RoleRepository;
import com.bit.usermanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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

        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();

        userRepository.save(AppUser.builder()
                        .name("Admin")
                        .username("admin")
                        .email("admin@gmail.com")
                        .password("admin")
                        .roles(List.of(adminRole)).build());
    }
}
