package com.bit.usermanagementservice.exception;

/**
 * Custom exception class for handling invalid roles.
 * This exception is thrown when a role provided is not valid or does not exist in the system.
 */
public class InvalidRoleException extends IllegalArgumentException {

    /**
     * Constructs a new InvalidRoleException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InvalidRoleException(String message) {
        super(message);
    }
}
