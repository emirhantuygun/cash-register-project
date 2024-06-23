package com.bit.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class represents a request for authentication.
 * It contains the username, password, and roles of the user.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserRequest {

    private String username;
    private String password;
    private List<String> roles;
}
