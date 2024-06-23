package com.bit.apigateway.exception;

/**
 * Custom exception class to handle scenarios where a token is not found.
 * This exception is thrown when a method expects a token but it is not provided or is invalid.
 */
public class TokenNotFoundException extends RuntimeException {

    /**
     * Constructs a new TokenNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public TokenNotFoundException(String message) {
        super(message);
    }
}