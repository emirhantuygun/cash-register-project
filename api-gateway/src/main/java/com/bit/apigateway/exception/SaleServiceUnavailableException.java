package com.bit.apigateway.exception;

public class SaleServiceUnavailableException extends RuntimeException {
    public SaleServiceUnavailableException(String message) {
        super(message);
    }
}