package com.bit.apigateway.exception;

public class InsufficientRolesException extends RuntimeException {
    public InsufficientRolesException(String message) {
        super(message);
    }
}