package com.bit.usermanagementservice.controller;

import com.bit.usermanagementservice.UserManagementServiceApplication;
import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LogManager.getLogger(UserManagementServiceApplication.class);


    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") Long id) {

        UserResponse userResponse = userService.getUser(id);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> userResponses = userService.getAllUsers();

        return new ResponseEntity<>(userResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<UserResponse>> getDeletedUsers() {

        List<UserResponse> deletedUserResponses = userService.getDeletedUsers();

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
        logger.info("Received request to fetch all users with filters and sorting: page={}, size={}, sortBy={}, direction={}, name={}, username={}, email={}",
                page, size, sortBy, direction, name, username, email);
        Page<UserResponse> users = userService.getAllUsersFilteredAndSorted(page, size, sortBy, direction, name, username, email, roleName);

        logger.info("Returning {} user responses filtered and sorted", users.getTotalElements());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {

        UserResponse userResponse = userService.createUser(userRequest);

        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                         @RequestBody @Valid UserRequest userRequest){
        UserResponse userResponse = userService.updateUser(id, userRequest);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<UserResponse> restoreUser(@PathVariable Long id){
        UserResponse userResponse = userService.restoreUser(id);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);

        return new ResponseEntity<>("User soft deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteUserPermanently(@PathVariable Long id){
        userService.deleteUserPermanently(id);

        return new ResponseEntity<>("User deleted permanently!", HttpStatus.OK);
    }

}
