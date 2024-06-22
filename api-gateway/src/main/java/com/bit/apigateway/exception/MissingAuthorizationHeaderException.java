package com.bit.apigateway.exception;

/**
 * Custom exception class to handle missing authorization header in API requests.
 * This exception is thrown when the authorization header is not present in the request.
 */
public class MissingAuthorizationHeaderException extends RuntimeException {
    public MissingAuthorizationHeaderException(String message) {
        super(message);
    }
}