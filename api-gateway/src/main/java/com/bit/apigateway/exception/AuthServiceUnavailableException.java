package com.bit.apigateway.exception;

/**
 * Custom exception class to represent an error when the authentication service is unavailable.
 * This exception is thrown when the system is unable to connect to the authentication service.
 */
public class AuthServiceUnavailableException extends RuntimeException {
    public AuthServiceUnavailableException(String message) {
        super(message);
    }
}
