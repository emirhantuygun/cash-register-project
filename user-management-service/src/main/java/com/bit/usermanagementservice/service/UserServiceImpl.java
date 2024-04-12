package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.UserManagementServiceApplication;
import com.bit.usermanagementservice.dto.AuthUserRequest;
import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.exception.InvalidRoleException;
import com.bit.usermanagementservice.exception.UserNotFoundException;
import com.bit.usermanagementservice.exception.UserNotSoftDeletedException;
import com.bit.usermanagementservice.model.AppUser;
import com.bit.usermanagementservice.model.Role;
import com.bit.usermanagementservice.repository.RoleRepository;
import com.bit.usermanagementservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("#{'${default-roles}'.split(', ')}")
    private final List<String> DEFAULT_ROLES;
    private static final Logger logger = LogManager.getLogger(UserManagementServiceApplication.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GatewayService gatewayService;

    @Override
    public UserResponse getUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        logger.info("User retrieved");
        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        List<AppUser> users = userRepository.findAll();

        logger.info("Retrieved {} users", users.size());
        return users.stream().map(this::mapToUserResponse).toList();
    }

    @Override
    public List<UserResponse> getDeletedUsers() {
        logger.info("Fetching all deleted users");
        List<AppUser> users = userRepository.findSoftDeletedUsers();

        logger.info("Retrieved {} deleted users", users.size());
        return users.stream().map(this::mapToUserResponse).toList();
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        logger.info("Creating a user");

        validateRoles(userRequest.getRoles());
        checkUniqueness(userRequest);

        gatewayService.createUser(mapToAuthUserRequest(userRequest));

        AppUser user = AppUser.builder()
                .name(userRequest.getName())
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .roles(getRolesAsRole(userRequest.getRoles()))
                .build();

        userRepository.save(user);

        logger.info("Created user with ID: {}", user.getId());
        return mapToUserResponseWithoutPassword(user);
    }


    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        logger.info("Updating user with ID {}: {}", id, userRequest);

        validateRoles(userRequest.getRoles());

        AppUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User doesn't exist with id " + id));

        checkUniquenessForUpdate(existingUser, userRequest);

        existingUser.setName(userRequest.getName());
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setPassword(userRequest.getPassword());
        existingUser.setRoles(getRolesAsRole(userRequest.getRoles()));


        userRepository.save(existingUser);

        gatewayService.updateUser(id, mapToAuthUserRequest(userRequest));

        logger.info("User updated with id {}", id);
        return mapToUserResponseWithoutPassword(existingUser);
    }

    @Override
    public UserResponse restoreUser(Long id) {
        if (!userRepository.isUserSoftDeleted(id)) {
            throw new UserNotSoftDeletedException("User with id " + id + " is not soft-deleted and cannot be restored.");
        }
        userRepository.restoreUser(id);
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Couldn't restore the user with id " + id));

        gatewayService.restoreUser(id);

        return mapToUserResponseWithoutPassword(user);
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);

        if(!userRepository.existsById(id))
           throw new UserNotFoundException("User not found with id " + id);

        userRepository.deleteById(id);

        gatewayService.deleteUser(id);

        logger.info("User soft-deleted");
    }

    @Override
    public void deleteUserPermanently(Long id) {
        if(!userRepository.existsById(id))
            throw new UserNotFoundException("User not found with id " + id);
        
        userRepository.deletePermanently(id);

        gatewayService.deleteUserPermanently(id);
    }

    private void checkUniqueness(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername()) || userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("Username or email already in use");
        }
    }
    private void checkUniquenessForUpdate(AppUser appUser, UserRequest userRequest){
        if (!userRequest.getUsername().equals(appUser.getUsername()) && userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DataIntegrityViolationException("Username already exists: " + userRequest.getUsername());
        }

        if (!userRequest.getEmail().equals(appUser.getEmail()) && userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists: " + userRequest.getEmail());
        }
    }
    private void validateRoles(List<String> roles) {

        for (String role : roles) {
            if (!DEFAULT_ROLES.contains(role)) {
                throw new InvalidRoleException("Invalid role: " + role);
            }
        }
    }
    private AuthUserRequest mapToAuthUserRequest (UserRequest userRequest) {
        return new AuthUserRequest(
                userRequest.getUsername(),
                userRequest.getPassword(),
                userRequest.getRoles());
    }
    private UserResponse mapToUserResponse(AppUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(getRolesAsString(user.getRoles()))
                .build();
    }
    private UserResponse mapToUserResponseWithoutPassword(AppUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(getRolesAsString(user.getRoles()))
                .build();
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
