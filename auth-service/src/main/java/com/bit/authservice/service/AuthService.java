package com.bit.authservice.service;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.AuthUserRequest;
import com.bit.authservice.wrapper.UpdateUserMessage;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AuthService {
  List<String> login(AuthRequest authRequest);

  void createUser(AuthUserRequest authUserRequest);

  List<String> refreshToken(HttpServletRequest request);

  void updateUser(Long id, AuthUserRequest authUserRequest);

  void updateUserWrapped(UpdateUserMessage updateUserMessage);

  void restoreUser(Long id);

  void deleteUser(Long id);

  void deleteUserPermanently(Long id);
}
