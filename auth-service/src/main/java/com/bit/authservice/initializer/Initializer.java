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

/**
 * This class is responsible for initializing default roles and users in the application.
 * It implements the CommandLineRunner interface to run the initialization logic when the application starts.
 *
 * @author Emirhan Tuygun
 */
@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    @Value("#{'${default-roles}'.split(', ')}")
    private final List<String> DEFAULT_ROLES;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * This method is responsible for running the initialization logic when the application starts.
     * It initializes default roles and users in the application.
     *
     * @param args Command line arguments. Not used in this method.
     */
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

    /**
     * This method initializes the default roles in the application.
     * It checks if a role with the given name exists in the database.
     * If the role does not exist, it saves a new Role entity with the given name.
     */
    private void initializeRoles() {
        for (String roleName : DEFAULT_ROLES) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    /**
     * This method initializes a cashier user in the application.
     * It retrieves the encoded password for the cashier user, fetches the cashier role from the database,
     * and saves a new AppUser entity with the cashier user details and the cashier role.
     */
    private void initializeCashierUser() {
        var encodedPassword = passwordEncoder.encode("cashier");
        Role cashierRole = roleRepository.findByRoleName("CASHIER").orElseThrow();

        userRepository.save(AppUser.builder()
                .username("cashier")
                .password(encodedPassword)
                .roles(List.of(cashierRole)).build());
    }

    /**
     * This method initializes a manager user in the application.
     * It retrieves the encoded password for the manager user, fetches the manager role from the database,
     * and saves a new AppUser entity with the manager user details and the manager role.
     */
    private void initializeManagerUser() {
        var encodedPassword = passwordEncoder.encode("manager");
        Role managerRole = roleRepository.findByRoleName("MANAGER").orElseThrow();

        userRepository.save(AppUser.builder()
                .username("manager")
                .password(encodedPassword)
                .roles(List.of(managerRole)).build());
    }

    /**
     * This method initializes an admin user in the application.
     * It retrieves the encoded password for the admin user, fetches the admin role from the database,
     * and saves a new AppUser entity with the admin user details and the admin role.
     */
    private void initializeAdminUser() {
        var encodedPassword = passwordEncoder.encode("admin");
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();

        userRepository.save(AppUser.builder()
                .username("admin")
                .password(encodedPassword)
                .roles(List.of(adminRole)).build());
    }

    /**
     * This method initializes a super user in the application.
     * It retrieves the encoded password for the super user, fetches the cashier, manager, and admin roles from the database,
     * and saves a new AppUser entity with the super user details and all three roles.
     */
    private void initializeSuperUser() {
        var encodedPassword = passwordEncoder.encode("super");
        Role cashierRole = roleRepository.findByRoleName("CASHIER").orElseThrow();
        Role managerRole = roleRepository.findByRoleName("MANAGER").orElseThrow();
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();

        userRepository.save(AppUser.builder()
                .username("super")
                .password(encodedPassword)
                .roles(List.of(cashierRole, managerRole, adminRole)).build());
    }

    /**
     * This method initializes additional users in the application.
     */
    private void initializeUsers() {

        for (int i = 5; i <= 20; i++) {
            String username = String.format("user%d", i);
            String password = String.format("user%d54", i);
            var encodedPassword = passwordEncoder.encode(password);

            Long roleId = 1L + i % 3;
            Role role = roleRepository.findById(roleId).orElseThrow();

            userRepository.save(AppUser.builder()
                    .username(username)
                    .password(encodedPassword)
                    .roles(List.of(role)).build());
        }
    }
}
