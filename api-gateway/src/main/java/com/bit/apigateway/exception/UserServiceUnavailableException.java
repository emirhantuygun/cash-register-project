package com.bit.apigateway.exception;

/**
 * This class represents a custom exception that is thrown when the user service is unavailable.
 * It extends the RuntimeException class, which means it does not require explicit catching.
 */
public class UserServiceUnavailableException extends RuntimeException {

    /**
     * Constructs a new UserServiceUnavailableException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public UserServiceUnavailableException(String message) {
        super(message);
    }
}