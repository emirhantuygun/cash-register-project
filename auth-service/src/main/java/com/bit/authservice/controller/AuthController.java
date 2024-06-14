package com.bit.authservice.controller;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthResponse;
import com.bit.authservice.dto.AuthStatus;
import com.bit.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

}
