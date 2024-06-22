package com.bit.apigateway.exception;

/**
 * Custom exception class to handle the scenario when a token is found to be logged out.
 * This exception is thrown when a request is made with an expired or invalid token.
 */
public class LoggedOutTokenException extends RuntimeException {
    public LoggedOutTokenException(String message) {
        super(message);
    }
}