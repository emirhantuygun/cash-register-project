package com.bit.apigateway.exception;

/**
 * Custom exception class to handle the scenario when a token is found to be logged out.
 * This exception is thrown when a request is made with an expired or invalid token.
 */
public class LoggedOutTokenException extends RuntimeException {

    /**
     * Constructs a new LoggedOutTokenException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public LoggedOutTokenException(String message) {
        super(message);
    }
}