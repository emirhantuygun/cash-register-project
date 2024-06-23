package com.bit.productservice.exception;

/**
 * Custom exception class to be thrown when a product is not soft deleted.
 * This exception is thrown when an attempt is made to perform operations on a product
 * that is not soft deleted, such as updating or deleting it.
 */
public class ProductNotSoftDeletedException extends RuntimeException {

    /**
     * Constructs a new ProductNotSoftDeletedException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     */
    public ProductNotSoftDeletedException(String message) {
        super(message);
    }

}
