package com.bit.authservice.service;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.exception.InvalidRefreshTokenException;
import com.bit.authservice.exception.RedisOperationException;
import com.bit.authservice.exception.UsernameExtractionException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * This interface represents the authentication service. It provides methods for login and refresh token operations.
 *
 * @author Emirhan Tuygun
 */
public interface AuthService {

  /**
   * Performs the login process for the given authentication request.
   *
   * @param authRequest The authentication request containing the username and password.
   * @return A list containing the access token and refresh token.
   * @throws RedisOperationException If there is an error while interacting with Redis.
   */
  List<String> login(AuthRequest authRequest) throws RedisOperationException;

  /**
   * Refreshes the access token using the provided refresh token.
   *
   * @param request The HTTP request containing the refresh token in the Authorization header.
   * @return A list containing the new access token and refresh token.
   * @throws InvalidRefreshTokenException If the refresh token is invalid or expired.
   * @throws UsernameExtractionException If the username cannot be extracted from the refresh token.
   * @throws RedisOperationException If there is an error while interacting with Redis.
   */
  List<String> refreshToken(HttpServletRequest request) throws InvalidRefreshTokenException, UsernameExtractionException, RedisOperationException;
}
