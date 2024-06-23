package com.bit.usermanagementservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * This class represents a UserResponse object. It is used to send user data in a response format.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String username;
    private String email;
    private String password;
    private List<String> roles;
}
