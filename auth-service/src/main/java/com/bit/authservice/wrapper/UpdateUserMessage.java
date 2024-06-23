package com.bit.authservice.wrapper;

import com.bit.authservice.dto.AuthUserRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a message for updating a user in the system.
 * It contains the user's ID and the updated user information.
 *
 * @author Emirhan Tuygun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserMessage {

    private Long id;
    private AuthUserRequest authUserRequest;
}
