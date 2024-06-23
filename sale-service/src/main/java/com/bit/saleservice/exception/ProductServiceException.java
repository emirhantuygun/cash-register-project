package com.bit.saleservice.exception;

/**
 * Custom exception class for product service related exceptions.
 * This class extends RuntimeException to allow unchecked exceptions.
 */
public class ProductServiceException extends RuntimeException  {

    /**
     * Constructs a new ProductServiceException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public ProductServiceException(String message) {
        super(message);
    }
}
