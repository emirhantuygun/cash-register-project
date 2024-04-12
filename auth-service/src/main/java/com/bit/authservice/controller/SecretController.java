package com.bit.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class SecretController {

    @GetMapping("/secret-admin")
    public ResponseEntity<String> getSecret() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UUID.randomUUID().toString());
    }

    @GetMapping("/secret-user")
    public ResponseEntity<String> getSecret2() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(UUID.randomUUID().toString());
    }
}
