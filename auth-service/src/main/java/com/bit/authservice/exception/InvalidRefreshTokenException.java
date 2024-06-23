package com.bit.authservice.exception;

/**
 * This class represents an exception that is thrown when an invalid refresh token is encountered.
 */
public class InvalidRefreshTokenException extends Exception {

    /**
     * Constructs a new InvalidRefreshTokenException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}