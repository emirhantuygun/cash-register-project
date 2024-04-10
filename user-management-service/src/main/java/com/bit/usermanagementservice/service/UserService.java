package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import java.util.List;

public interface UserService {

    UserResponse getUser(Long id);
    List<UserResponse> getAllUsers();
    List<UserResponse> getDeletedUsers();
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
    UserResponse restoreUser(Long id);
    void deleteUserPermanently(Long id);
}
