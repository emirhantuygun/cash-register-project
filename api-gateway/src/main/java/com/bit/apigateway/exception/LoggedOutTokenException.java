package com.bit.apigateway.exception;

public class LoggedOutTokenException extends RuntimeException {
    public LoggedOutTokenException(String message) {
        super(message);
    }
}