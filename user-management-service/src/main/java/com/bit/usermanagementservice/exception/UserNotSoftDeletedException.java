package com.bit.usermanagementservice.exception;

public class UserNotSoftDeletedException extends RuntimeException {
    public UserNotSoftDeletedException(String message) {
        super(message);
    }
}
