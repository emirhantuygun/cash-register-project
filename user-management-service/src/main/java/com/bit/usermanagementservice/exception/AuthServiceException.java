package com.bit.usermanagementservice.exception;

/**
 * Custom exception class for handling authentication service related exceptions.
 * This class extends RuntimeException to allow unchecked exceptions.
 */
public class AuthServiceException extends RuntimeException {

    /**
     * Constructs a new AuthServiceException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public AuthServiceException(String message) {
        super(message);
    }
}
