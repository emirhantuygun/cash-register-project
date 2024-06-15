package com.bit.reportservice.exception;

public class ReceiptGenerationException extends Exception {
    public ReceiptGenerationException(String message) {
        super(message);
    }

    public ReceiptGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}