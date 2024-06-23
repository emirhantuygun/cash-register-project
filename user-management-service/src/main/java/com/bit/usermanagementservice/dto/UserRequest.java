package com.bit.usermanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class represents a user request object. It is used to encapsulate the necessary data for creating a new user.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Name is required and should not be blank!")
    private String name;

    @NotBlank(message = "Username is required and should not be blank!")
    private String username;

    @NotBlank(message = "Email is required and should not be blank!")
    private String email;

    @NotBlank(message = "Password is required and should not be blank!")
    private String password;
    private List<String> roles;
}
