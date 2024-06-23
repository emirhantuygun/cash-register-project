package com.bit.apigateway.exception;

/**
 * Custom exception class to handle insufficient roles during API Gateway access.
 * This exception is thrown when a user does not have the necessary roles to access a specific API.
 */
public class InsufficientRolesException extends RuntimeException {

    /**
     * Constructs a new instance of {@code InsufficientRolesException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InsufficientRolesException(String message) {
        super(message);
    }
}