package com.bit.authservice.dto;

/**
 * Represents the response with an access token, a refresh token and an auth status.
 *
 * @param accessToken  An access token
 * @param refreshToken A refresh token
 * @param authStatus The auth status such as LOGIN_SUCCESS
 *
 * @author Emirhan Tuygun
 */
public record AuthResponse (String accessToken, String refreshToken, AuthStatus authStatus) {
}
