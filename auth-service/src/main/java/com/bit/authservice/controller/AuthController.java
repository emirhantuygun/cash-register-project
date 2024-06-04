package com.bit.authservice.controller;

import com.bit.authservice.config.RabbitMQConfig;
import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthResponse;
import com.bit.authservice.dto.AuthStatus;
import com.bit.authservice.dto.UserRequest;
import com.bit.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            var tokens = authService.login(authRequest);
            var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.LOGIN_SUCCESS);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(authResponse);

        } catch (Exception e) {

            var authResponse = new AuthResponse(null, null, AuthStatus.LOGIN_FAILED);

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(authResponse);
        }
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {

        try {
            var tokens = authService.refreshToken(request);

            var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.TOKEN_REFRESHED_SUCCESSFULLY);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(authResponse);

        } catch (Exception e) {
            var authResponse = new AuthResponse(null, null, AuthStatus.TOKEN_REFRESH_FAILED);

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(authResponse);
        }
    }

    @PostMapping("/create")
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public ResponseEntity<String> createUser(@RequestBody UserRequest userRequest) {
        try {
            authService.createUser(userRequest);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("User created successfully!");

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("User not created!");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        authService.updateUser(id, userRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("User updated successfully!");
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<String> restoreUser(@PathVariable Long id){
        authService.restoreUser(id);

        return new ResponseEntity<>("User restored successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        authService.deleteUser(id);

        return new ResponseEntity<>("User soft deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/delete/permanent/{id}")
    public ResponseEntity<String> deleteUserPermanently(@PathVariable Long id){
        authService.deleteUserPermanently(id);

        return new ResponseEntity<>("User deleted permanently!", HttpStatus.OK);
    }
}
