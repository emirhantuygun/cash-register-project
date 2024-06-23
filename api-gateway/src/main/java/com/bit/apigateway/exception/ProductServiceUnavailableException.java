package com.bit.apigateway.exception;

/**
 * Custom exception class to represent a situation when the product service is unavailable.
 * This exception is thrown when the API Gateway fails to connect to the product service.
 */
public class ProductServiceUnavailableException extends RuntimeException {

    /**
     * Constructs a new ProductServiceUnavailableException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public ProductServiceUnavailableException(String message) {
        super(message);
    }
}