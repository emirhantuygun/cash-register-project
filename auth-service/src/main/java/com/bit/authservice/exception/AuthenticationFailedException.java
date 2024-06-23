package com.bit.authservice.exception;

/**
 * Custom exception class for authentication failures.
 * This exception is thrown when the authentication process fails.
 */
public class AuthenticationFailedException extends RuntimeException {

    /**
     * Constructs a new AuthenticationFailedException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public AuthenticationFailedException(String message) {
        super(message);
    }
}