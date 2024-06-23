package com.bit.usermanagementservice.exception;

/**
 * Custom exception class to be thrown when a user is not soft deleted.
 * This exception is thrown when an attempt is made to perform an operation
 * on a user that is not soft deleted, such as updating or deleting.
 */
public class UserNotSoftDeletedException extends RuntimeException {

    /**
     * Constructs a new UserNotSoftDeletedException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     */
    public UserNotSoftDeletedException(String message) {
        super(message);
    }
}
