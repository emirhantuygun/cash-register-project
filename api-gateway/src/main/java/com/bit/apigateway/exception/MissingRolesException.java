package com.bit.apigateway.exception;

/**
 * Custom exception class to handle missing roles during API Gateway authorization.
 * This exception is thrown when a user does not have the required roles to access a specific API.
 */
public class MissingRolesException extends RuntimeException {
    public MissingRolesException(String message) {
        super(message);
    }
}