package com.bit.authservice.wrapper;

import com.bit.authservice.dto.UserRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserMessage {
    private Long id;
    private UserRequest userRequest;

}
