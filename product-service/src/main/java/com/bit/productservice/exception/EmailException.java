package com.bit.productservice.exception;

/**
 * Custom exception class for handling email related exceptions.
 * This class extends RuntimeException to allow unchecked exceptions.
 */
public class EmailException extends RuntimeException {

    /**
     * Constructs a new EmailException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public EmailException(String message) {
        super(message);
    }
}