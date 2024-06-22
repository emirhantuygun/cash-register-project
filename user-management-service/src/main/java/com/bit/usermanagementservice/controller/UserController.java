package com.bit.usermanagementservice.controller;

import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") Long id) {
        log.trace("Entering getUser method in UserController with id: {}", id);

        UserResponse userResponse = userService.getUser(id);
        log.info("Retrieved user details for id: {}", id);

        log.trace("Exiting getUser method in UserController");
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.trace("Entering getAllUsers method in UserController");

        List<UserResponse> userResponses = userService.getAllUsers();
        log.info("Retrieved all users");

        log.trace("Exiting getAllUsers method in UserController");
        return new ResponseEntity<>(userResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<UserResponse>> getDeletedUsers() {
        log.trace("Entering getDeletedUsers method in UserController");

        List<UserResponse> deletedUserResponses = userService.getDeletedUsers();
        log.info("Retrieved deleted users");

        log.trace("Exiting getDeletedUsers method in UserController");
        return new ResponseEntity<>(deletedUserResponses, HttpStatus.OK);
    }

    @GetMapping("/filteredAndSorted")
    public ResponseEntity<Page<UserResponse>> getAllUsersFilteredAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String roleName
    ) {
        log.trace("Entering getAllUsersFilteredAndSorted method in UserController with page: {}, size: {}, sortBy: {}, direction: {}, name: {}, username: {}, email: {}, roleName: {}",
                page, size, sortBy, direction, name, username, email, roleName);

        Page<UserResponse> users = userService.getAllUsersFilteredAndSorted(page, size, sortBy, direction, name, username, email, roleName);
        log.info("Retrieved filtered and sorted users");

        log.trace("Exiting getAllUsersFilteredAndSorted method in UserController");
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        log.trace("Entering createUser method in UserController with userRequest: {}", userRequest);

        UserResponse userResponse = userService.createUser(userRequest);
        log.info("Created new user with details: {}", userResponse);

        log.trace("Exiting createUser method in UserController");
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                         @RequestBody @Valid UserRequest userRequest){
        log.trace("Entering updateUser method in UserController with id: {}, userRequest: {}", id, userRequest);

        UserResponse userResponse = userService.updateUser(id, userRequest);
        log.info("Updated user with id: {}", id);

        log.trace("Exiting updateUser method in UserController");
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<UserResponse> restoreUser(@PathVariable Long id){
        log.trace("Entering restoreUser method in UserController with id: {}", id);

        UserResponse userResponse = userService.restoreUser(id);
        log.info("Restored user with id: {}", id);

        log.trace("Exiting restoreUser method in UserController");
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        log.trace("Entering deleteUser method in UserController with id: {}", id);

        userService.deleteUser(id);
        log.info("Soft deleted user with id: {}", id);

        log.trace("Exiting deleteUser method in UserController");
        return new ResponseEntity<>("User soft deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteUserPermanently(@PathVariable Long id){
        log.trace("Entering deleteUserPermanently method in UserController with id: {}", id);

        userService.deleteUserPermanently(id);
        log.info("Permanently deleted user with id: {}", id);

        log.trace("Exiting deleteUserPermanently method in UserController");
        return new ResponseEntity<>("User deleted permanently!", HttpStatus.OK);
    }
}
