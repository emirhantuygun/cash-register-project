package com.bit.usermanagementservice.wrapper;

import com.bit.usermanagementservice.dto.AuthUserRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserMessage {
    private Long id;
    private AuthUserRequest authUserRequest;

}
