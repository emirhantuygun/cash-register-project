package com.bit.apigateway.exception;

/**
 * Custom exception class to represent a situation when the product service is unavailable.
 * This exception is thrown when the API Gateway fails to connect to the product service.
 */
public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(String message) {
        super(message);
    }
}