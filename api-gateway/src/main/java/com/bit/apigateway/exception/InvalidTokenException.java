package com.bit.apigateway.exception;

/**
 * This class represents an exception that is thrown when an invalid token is encountered.
 * It extends the RuntimeException class, which means it does not require explicit catching.
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidTokenException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}