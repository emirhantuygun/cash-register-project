package com.bit.apigateway.exception;

/**
 * Custom exception class to represent an error when the authentication service is unavailable.
 * This exception is thrown when the system is unable to connect to the authentication service.
 */
public class AuthServiceUnavailableException extends RuntimeException {

    /**
     * Constructs a new AuthServiceUnavailableException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public AuthServiceUnavailableException(String message) {
        super(message);
    }
}
