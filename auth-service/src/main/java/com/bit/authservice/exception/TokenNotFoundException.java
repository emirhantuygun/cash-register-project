package com.bit.authservice.exception;

/**
 * Custom exception class for handling cases when a token is not found.
 * This exception is thrown when a method requires a token but it is not provided or not found in the system.
 */
public class TokenNotFoundException extends RuntimeException {

    /**
     * Constructs a new TokenNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public TokenNotFoundException(String message) {
        super(message);
    }
}