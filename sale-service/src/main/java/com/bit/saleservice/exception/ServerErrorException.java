package com.bit.saleservice.exception;

/**
 * Custom exception class to represent server errors.
 * This exception is thrown when there is an error on the server side.
 */
public class ServerErrorException extends RuntimeException {

    /**
     * Constructs a new ServerErrorException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public ServerErrorException(String message) {
        super(message);
    }
}