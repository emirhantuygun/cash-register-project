package com.bit.apigateway.exception;

/**
 * This class represents an exception that is thrown when an invalid token is encountered.
 * It extends the RuntimeException class, which means it does not require explicit catching.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}