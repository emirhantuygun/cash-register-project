package com.bit.authservice.dto;


public record AuthResponse (String accessToken, String refreshToken, AuthStatus authStatus) {
}
