package com.bit.authservice.service;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.exception.InvalidRefreshTokenException;
import com.bit.authservice.exception.RedisOperationException;
import com.bit.authservice.exception.UsernameExtractionException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AuthService {
  List<String> login(AuthRequest authRequest) throws RedisOperationException;

  List<String> refreshToken(HttpServletRequest request) throws InvalidRefreshTokenException, UsernameExtractionException, RedisOperationException;

}
