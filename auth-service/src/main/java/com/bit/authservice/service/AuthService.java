package com.bit.authservice.service;

import com.bit.authservice.dto.AuthRequest;
import com.bit.authservice.dto.UserRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface AuthService {
  List<String> login(AuthRequest authRequest);

  void createUser(UserRequest userRequest);

  List<String> refreshToken(HttpServletRequest request);

  void updateUser(Long id, UserRequest userRequest);

  void restoreUser(Long id);

  void deleteUser(Long id);

  void deleteUserPermanently(Long id);
}
