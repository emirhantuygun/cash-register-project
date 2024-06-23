package com.bit.authservice.exception;

/**
 * Custom exception class for handling errors related to extracting usernames.
 * This exception is thrown when there is an issue with extracting the username from a given input.
 */
public class UsernameExtractionException extends Exception {

    /**
     * Constructs a new UsernameExtractionException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public UsernameExtractionException(String message) {
        super(message);
    }
}