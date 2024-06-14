package com.bit.apigateway.exception;

public class MissingRolesException extends RuntimeException {
    public MissingRolesException(String message) {
        super(message);
    }
}