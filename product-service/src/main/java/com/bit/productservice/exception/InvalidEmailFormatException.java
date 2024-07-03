package com.bit.productservice.exception;

public class InvalidEmailFormatException extends EmailException {
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}