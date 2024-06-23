package com.bit.usermanagementservice.exception;

/**
 * Custom exception class for handling user not found scenarios.
 * This exception is thrown when a user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
