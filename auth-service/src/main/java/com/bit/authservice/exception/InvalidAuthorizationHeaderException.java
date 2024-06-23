package com.bit.authservice.exception;

/**
 * This class represents an exception that is thrown when the authorization header in a request is invalid.
 */
public class InvalidAuthorizationHeaderException extends RuntimeException {

    /**
     * Constructs a new InvalidAuthorizationHeaderException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InvalidAuthorizationHeaderException(String message) {
        super(message);
    }
}