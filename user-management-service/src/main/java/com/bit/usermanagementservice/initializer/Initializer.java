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

import java.math.BigDecimal;
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
        initializeCashierUser();
        initializeManagerUser();
        initializeAdminUser();
        initializeSuperUser();
        initializeUsers();
    }

    private void initializeRoles() {
        for (String roleName : DEFAULT_ROLES) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    private void initializeCashierUser() {

        Role cashierRole = roleRepository.findByRoleName("CASHIER").orElseThrow();

        userRepository.save(AppUser.builder()
                .name("Cashier")
                .username("cashier")
                .email("cashier@gmail.com")
                .password("cashier")
                .roles(List.of(cashierRole)).build());
    }

    private void initializeManagerUser() {

        Role managerRole = roleRepository.findByRoleName("MANAGER").orElseThrow();

        userRepository.save(AppUser.builder()
                .name("Manager")
                .username("manager")
                .email("manager@gmail.com")
                .password("manager")
                .roles(List.of(managerRole)).build());
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

    private void initializeSuperUser() {

        Role cashierRole = roleRepository.findByRoleName("CASHIER").orElseThrow();
        Role managerRole = roleRepository.findByRoleName("MANAGER").orElseThrow();
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();

        userRepository.save(AppUser.builder()
                .name("Super")
                .username("super")
                .email("super@gmail.com")
                .password("super")
                .roles(List.of(cashierRole, managerRole, adminRole)).build());
    }

    private void initializeUsers() {

        for (int i = 5; i <= 20; i++) {
            String name = String.format("User %d", i);
            String username = String.format("user%d", i);
            String email = String.format("user%d@gmail.com", i);
            String password = String.format("user%d54", i);

            Long roleId = 1L + i % 3;
            Role role = roleRepository.findById(roleId).orElseThrow();

            AppUser user = AppUser.builder()
                    .name(name)
                    .username(username)
                    .email(email)
                    .password(password)
                    .roles(List.of(role)).build();

            userRepository.save(user);
        }
    }
}
