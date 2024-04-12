package com.bit.usermanagementservice.exception;

public class InvalidRoleException extends IllegalArgumentException {
    public InvalidRoleException(String message) {
        super(message);
    }
}
