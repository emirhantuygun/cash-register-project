package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.usermanagementservice.dto.AuthUserRequest;
import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.entity.AppUser;
import com.bit.usermanagementservice.entity.Role;
import com.bit.usermanagementservice.exception.InvalidRoleException;
import com.bit.usermanagementservice.exception.RabbitMQException;
import com.bit.usermanagementservice.exception.UserNotFoundException;
import com.bit.usermanagementservice.exception.UserNotSoftDeletedException;
import com.bit.usermanagementservice.repository.RoleRepository;
import com.bit.usermanagementservice.repository.UserRepository;
import com.bit.usermanagementservice.wrapper.UpdateUserMessage;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    @Value("#{'${default-roles}'.split(', ')}")
    private final List<String> DEFAULT_ROLES;

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.routingKey.create}")
    private String ROUTING_KEY_CREATE;

    @Value("${rabbitmq.routingKey.update}")
    private String ROUTING_KEY_UPDATE;

    @Value("${rabbitmq.routingKey.delete}")
    private String ROUTING_KEY_DELETE;

    @Value("${rabbitmq.routingKey.deletePermanent}")
    private String ROUTING_KEY_DELETE_PERMANENT;

    @Value("${rabbitmq.routingKey.restore}")
    private String ROUTING_KEY_RESTORE;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public UserResponse getUser(Long id) {
        log.trace("Entering getUser method in UserServiceImpl with id: {}", id);

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new UserNotFoundException("User not found with id " + id);
                });
        log.info("User found with id: {}", id);

        log.trace("Exiting getUser method in UserServiceImpl");
        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.trace("Entering getAllUsers method in UserServiceImpl");

        List<AppUser> users = userRepository.findAll();
        log.info("Retrieved {} users", users.size());

        log.trace("Exiting getAllUsers method in UserServiceImpl");
        return users.stream().map(this::mapToUserResponse).toList();
    }

    @Override
    public List<UserResponse> getDeletedUsers() {
        log.trace("Entering getDeletedUsers method in UserServiceImpl");

        List<AppUser> users = userRepository.findSoftDeletedUsers();
        log.info("Retrieved {} deleted users", users.size());

        log.trace("Exiting getDeletedUsers method in UserServiceImpl");
        return users.stream().map(this::mapToUserResponse).toList();
    }

    @Override
    public Page<UserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String name, String username, String email, String roleName) {
        log.trace("Entering getAllUsersFilteredAndSorted method in UserServiceImpl with parameters: page={}, size={}, sortBy={}, direction={}, name={}, username={}, email={}, roleName={}",
                page, size, sortBy, direction, name, username, email, roleName);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);

        Page<AppUser> usersPage = userRepository.findAll((root, query, criteriaBuilder) -> {

            List<Predicate> predicates = getPredicates(name, username, email, roleName, root, criteriaBuilder);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        log.info("Retrieved {} users", usersPage.getTotalElements());

        log.trace("Exiting getAllUsersFilteredAndSorted method in UserServiceImpl");
        return usersPage.map(this::mapToUserResponse);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.trace("Entering createUser method in UserServiceImpl with userRequest: {}", userRequest);

        validateRoles(userRequest.getRoles());
        checkUniqueness(userRequest);

        AppUser user = AppUser.builder()
                .name(userRequest.getName())
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .roles(getRolesAsRole(userRequest.getRoles()))
                .build();


        try {
            AuthUserRequest authUserRequest = mapToAuthUserRequest(userRequest);
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CREATE, authUserRequest);
            log.info("Sent create message to RabbitMQ for user: {}", userRequest.getUsername());

        } catch (Exception e) {
            log.error("Failed to send create message to RabbitMQ for user: {}", userRequest.getUsername(), e);
            throw new RabbitMQException("Failed to send create message to RabbitMQ", e);
        }

        userRepository.save(user);
        log.info("User created with username: {}", userRequest.getUsername());

        log.trace("Exiting createUser method in UserServiceImpl");
        return mapToUserResponseWithoutPassword(user);
    }


    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.trace("Entering updateUser method in UserServiceImpl with id: {} and userRequest: {}", id, userRequest);

        validateRoles(userRequest.getRoles());

        AppUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User does not exist with id " + id);
                    return new UserNotFoundException("User doesn't exist with id " + id);
                });

        checkUniquenessForUpdate(existingUser, userRequest);

        existingUser.setName(userRequest.getName());
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setPassword(userRequest.getPassword());
        existingUser.setRoles(getRolesAsRole(userRequest.getRoles()));

        try {
            AuthUserRequest authUserRequest = mapToAuthUserRequest(userRequest);
            UpdateUserMessage updateUserMessage = new UpdateUserMessage(id, authUserRequest);
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_UPDATE, updateUserMessage);
            log.info("Sent update message to RabbitMQ for user: {}", userRequest.getUsername());

        } catch (Exception e) {
            log.error("Failed to send update message to RabbitMQ for user: {}", userRequest.getUsername(), e);
            throw new RabbitMQException("Failed to send update message to RabbitMQ", e);
        }

        userRepository.save(existingUser);
        log.info("User updated with id: {}", id);

        log.trace("Exiting updateUser method in UserServiceImpl");
        return mapToUserResponseWithoutPassword(existingUser);
    }

    @Override
    @Transactional
    public UserResponse restoreUser(Long id) {
        log.trace("Entering restoreUser method in UserServiceImpl with id: {}", id);

        if (!userRepository.existsByIdAndDeletedTrue(id)) {
            log.warn("User with id {} is not soft-deleted and cannot be restored.", id);
            throw new UserNotSoftDeletedException("User with id " + id + " is not soft-deleted and cannot be restored.");
        }

        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_RESTORE, id);
            log.info("Sent restore message to RabbitMQ for user with id: {}", id);

        } catch (Exception e) {
            log.error("Failed to send restore message to RabbitMQ for user with id: {}", id, e);
            throw new RabbitMQException("Failed to send restore message to RabbitMQ", e);
        }

        userRepository.restoreUser(id);
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Couldn't restore the user with id " + id);
                    return new UserNotFoundException("Couldn't restore the user with id " + id);
                });
        log.info("User restored with id: {}", id);

        log.trace("Exiting restoreUser method in UserServiceImpl");
        return mapToUserResponseWithoutPassword(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.trace("Entering deleteUser method in UserServiceImpl with id: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("User not found with id: {}", id);
            throw new UserNotFoundException("User not found with id " + id);
        }

        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_DELETE, id);
            log.info("Sent delete message to RabbitMQ for user with id: {}", id);

        } catch (Exception e) {
            log.error("Failed to send delete message to RabbitMQ for user with id: {}", id, e);
            throw new RabbitMQException("Failed to send delete message to RabbitMQ", e);
        }

        userRepository.deleteById(id);
        log.info("User soft deleted with id: {}", id);

        log.trace("Exiting deleteUser method in UserServiceImpl");
    }

    @Override
    @Transactional
    public void deleteUserPermanently(Long id) {
        log.trace("Entering deleteUserPermanently method in UserServiceImpl with id: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("User not found with id: {}", id);
            throw new UserNotFoundException("User not found with id " + id);
        }

        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_DELETE_PERMANENT, id);
            log.info("Sent delete-permanent message to RabbitMQ for user with id: {}", id);

        } catch (Exception e) {
            log.error("Failed to send delete-permanent message to RabbitMQ for user with id: {}", id, e);
            throw new RabbitMQException("Failed to send delete-permanent message to RabbitMQ", e);
        }

        userRepository.deleteRolesForUser(id);
        userRepository.deletePermanently(id);
        log.info("User permanently deleted with id: {}", id);

        log.trace("Exiting deleteUserPermanently method in UserServiceImpl");
    }

    @ExcludeFromGeneratedCoverage
    private List<Predicate> getPredicates(String name, String username, String email, String roleName, Root<AppUser> root, CriteriaBuilder criteriaBuilder) {
        log.trace("Entering getPredicates method in UserServiceImpl with parameters: name={}, username={}, email={}, roleName={}", name, username, email, roleName);

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(name)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(username)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(email)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(roleName)) {
            Join<AppUser, Role> roleJoin = root.join("roles", JoinType.INNER);
            predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(roleJoin.get("roleName")), roleName.toLowerCase()));
        }
        log.info("Created {} predicates for filtering", predicates.size());

        log.trace("Exiting getPredicates method in UserServiceImpl");
        return predicates;
    }

    @ExcludeFromGeneratedCoverage
    private void checkUniqueness(UserRequest userRequest) {
        log.trace("Entering checkUniqueness method in UserServiceImpl with userRequest: {}", userRequest);

        if (userRepository.existsByUsername(userRequest.getUsername())) {
            log.warn("Username {} is already taken", userRequest.getUsername());
            throw new IllegalArgumentException("Username is already taken: " + userRequest.getUsername());
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Email {} is already taken", userRequest.getEmail());
            throw new IllegalArgumentException("Email is already taken: " + userRequest.getEmail());
        }

        log.trace("Exiting checkUniqueness method in UserServiceImpl");
    }

    @ExcludeFromGeneratedCoverage
    private void checkUniquenessForUpdate(AppUser appUser, UserRequest userRequest) {
        log.trace("Entering checkUniquenessForUpdate method in UserServiceImpl with existingUser: {} and userRequest: {}", appUser, userRequest);

        if (!userRequest.getUsername().equals(appUser.getUsername()) && userRepository.existsByUsername(userRequest.getUsername())) {
            log.warn("Username {} is already taken by another user", userRequest.getUsername());
            throw new DataIntegrityViolationException("Username already exists: " + userRequest.getUsername());
        }

        if (!userRequest.getEmail().equals(appUser.getEmail()) && userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Email {} is already taken by another user", userRequest.getEmail());
            throw new DataIntegrityViolationException("Email already exists: " + userRequest.getEmail());
        }

        log.trace("Exiting checkUniquenessForUpdate method in UserServiceImpl");
    }

    @ExcludeFromGeneratedCoverage
    protected void validateRoles(List<String> roles) {
        log.trace("Entering validateRoles method in UserServiceImpl with roleNames");

        for (String role : roles) {
            if (!DEFAULT_ROLES.contains(role)) {
                log.error("Invalid role: " + role);
                throw new InvalidRoleException("Invalid role: " + role);
            }
        }

        log.trace("Exiting validateRoles method in UserServiceImpl");
    }

    private AuthUserRequest mapToAuthUserRequest(UserRequest userRequest) {
        log.trace("Entering mapToAuthUserRequest method in UserServiceImpl with userRequest: {}", userRequest);

        log.trace("Exiting mapToAuthUserRequest method in UserServiceImpl");
        return new AuthUserRequest(
                userRequest.getUsername(),
                userRequest.getPassword(),
                userRequest.getRoles());
    }

    private UserResponse mapToUserResponse(AppUser user) {
        log.trace("Entering mapToUserResponse method in UserServiceImpl with user: {}", user);

        log.trace("Exiting mapToUserResponse method in UserServiceImpl");
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
        log.trace("Entering mapToUserResponseWithoutPassword method in UserServiceImpl with user: {}", user);

        log.trace("Exiting mapToUserResponseWithoutPassword method in UserServiceImpl");
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(getRolesAsString(user.getRoles()))
                .build();
    }

    private List<String> getRolesAsString(List<Role> roles) {
        log.trace("Entering getRolesAsString method in UserServiceImpl with roleNames");

        log.trace("Exiting getRolesAsString method in UserServiceImpl with roleNames");
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    private List<Role> getRolesAsRole(List<String> roles) {
        log.trace("Entering getRolesAsRole method in UserServiceImpl with roleNames");

        List<Role> rolesList = new ArrayList<>();
        roles.forEach(roleName -> {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> {
                        log.error("Role " + roleName + " not found");
                        return new RuntimeException("Role " + roleName + " not found");
                    });
            rolesList.add(role);
        });

        log.trace("Exiting getRolesAsRole method in UserServiceImpl");
        return rolesList;
    }
}
