package com.bit.apigateway.exception;

/**
 * Custom exception class to handle missing roles during API Gateway authorization.
 * This exception is thrown when a user does not have the required roles to access a specific API.
 */
public class MissingRolesException extends RuntimeException {

    /**
     * Constructs a new instance of {@code MissingRolesException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public MissingRolesException(String message) {
        super(message);
    }
}