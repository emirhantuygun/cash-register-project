package com.bit.usermanagementservice.controller;

import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.bit.usermanagementservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
    public ResponseEntity<Page<UserResponse>> getAllUsersFilteredAndSorted() {
        return null;
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

        return new ResponseEntity<>("User safe deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteUserPermanently(@PathVariable Long id){
        userService.deleteUserPermanently(id);

        return new ResponseEntity<>("User deleted permanently!", HttpStatus.OK);
    }

}
