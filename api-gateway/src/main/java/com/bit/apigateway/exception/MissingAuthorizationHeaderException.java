package com.bit.apigateway.exception;

/**
 * Custom exception class to handle missing authorization header in API requests.
 * This exception is thrown when the authorization header is not present in the request.
 */
public class MissingAuthorizationHeaderException extends RuntimeException {

    /**
     * Constructs a new instance of {@code MissingAuthorizationHeaderException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public MissingAuthorizationHeaderException(String message) {
        super(message);
    }
}