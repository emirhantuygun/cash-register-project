package com.bit.reportservice.exception;

public class HeaderProcessingException extends Exception {
    public HeaderProcessingException(String message) {
        super(message);
    }

    public HeaderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}