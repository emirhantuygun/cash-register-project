package com.bit.usermanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AuthUserRequest {

    private String username;
    private String password;
    private List<String> roles;
}
