package com.bit.saleservice.exception;

/**
 * Custom exception class for handling scenarios when a product is not found.
 * This exception is thrown when a product-related operation is attempted on a product that does not exist.
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructs a new ProductNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public ProductNotFoundException(String message) {
        super(message);
    }
}