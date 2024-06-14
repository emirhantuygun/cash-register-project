package com.bit.authservice.exception;

public class MissingAuthorizationHeaderException extends RuntimeException {
    public MissingAuthorizationHeaderException(String message) {
        super(message);
    }
}