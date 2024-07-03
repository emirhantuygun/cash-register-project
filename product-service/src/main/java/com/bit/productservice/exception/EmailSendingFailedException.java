package com.bit.productservice.exception;

public class EmailSendingFailedException extends EmailException {
    public EmailSendingFailedException(String message) {
        super(message);
    }
}