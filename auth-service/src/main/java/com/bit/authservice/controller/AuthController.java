package com.bit.authservice.controller;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthResponse;
import com.bit.authservice.dto.AuthStatus;
import com.bit.authservice.exception.InvalidRefreshTokenException;
import com.bit.authservice.exception.RedisOperationException;
import com.bit.authservice.exception.UsernameExtractionException;
import com.bit.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) throws RedisOperationException {
        log.trace("Entering login method in AuthController");

        var tokens = authService.login(authRequest);
        var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.LOGIN_SUCCESS);

        log.trace("Exiting login method in AuthController");
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) throws InvalidRefreshTokenException, RedisOperationException, UsernameExtractionException {
        log.trace("Entering refreshToken method in AuthController");

        var tokens = authService.refreshToken(request);
        var authResponse = new AuthResponse(tokens.get(0), tokens.get(1), AuthStatus.TOKEN_REFRESHED_SUCCESSFULLY);

        log.trace("Exiting refreshToken method in AuthController");
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }
}
