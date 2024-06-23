package com.bit.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an authentication request with username and password.
 * This class is used to encapsulate the necessary data for authentication.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Username is required and should not be blank!")
    private String username;

    @NotBlank(message = "Password is required and should not be blank!")
    private String password;
}
