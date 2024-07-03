package com.bit.productservice.exception;

public class MissingEmailConfigurationException extends EmailException {
    public MissingEmailConfigurationException(String message) {
        super(message);
    }
}