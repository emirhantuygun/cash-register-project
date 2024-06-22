package com.bit.apigateway.exception;

/**
 * Custom exception class to handle situations when the Sale Service is unavailable.
 * This exception is thrown when a request to the Sale Service cannot be fulfilled due to
 * temporary unavailability or other reasons.
 */
public class SaleServiceUnavailableException extends RuntimeException {
    public SaleServiceUnavailableException(String message) {
        super(message);
    }
}