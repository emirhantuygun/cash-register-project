package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.dto.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    UserResponse getUser(Long id);
    List<UserResponse> getAllUsers();
    List<UserResponse> getDeletedUsers();
    Page<UserResponse> getAllUsersFilteredAndSorted(int page, int size, String sortBy, String direction, String name, String username, String email, String roleName);
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
    UserResponse restoreUser(Long id);
    void deleteUserPermanently(Long id);
}
