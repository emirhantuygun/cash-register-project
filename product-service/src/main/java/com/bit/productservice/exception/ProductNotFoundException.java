package com.bit.productservice.exception;

/**
 * Custom exception class for handling scenarios when a product is not found.
 * This exception is thrown when a product with a specific ID is not found in the system.
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructs a new ProductNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public ProductNotFoundException(String message) {
        super(message);
    }
}