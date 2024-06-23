package com.bit.authservice.exception;

/**
 * Custom exception class to be thrown when a role is not found in the system.
 */
public class RoleNotFoundException extends Exception {

    /**
     * Constructs a new RoleNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public RoleNotFoundException(String message) {
        super(message);
    }
}