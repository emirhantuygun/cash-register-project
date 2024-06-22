package com.bit.apigateway.exception;

/**
 * Custom exception class to handle insufficient roles during API Gateway access.
 * This exception is thrown when a user does not have the necessary roles to access a specific API.
 */
public class InsufficientRolesException extends RuntimeException {
    public InsufficientRolesException(String message) {
        super(message);
    }
}