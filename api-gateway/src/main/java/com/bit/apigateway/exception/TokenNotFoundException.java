package com.bit.apigateway.exception;

/**
 * Custom exception class to handle scenarios where a token is not found.
 * This exception is thrown when a method expects a token but it is not provided or is invalid.
 */
public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}