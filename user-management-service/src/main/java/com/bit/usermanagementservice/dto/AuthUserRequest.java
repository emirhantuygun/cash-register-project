package com.bit.usermanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * This class represents a request for user authentication.
 * It contains the username, password, and a list of roles for the user.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@AllArgsConstructor
public class AuthUserRequest {

    private String username;
    private String password;
    private List<String> roles;
}
