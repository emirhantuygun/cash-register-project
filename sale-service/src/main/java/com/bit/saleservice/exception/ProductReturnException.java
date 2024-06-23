package com.bit.saleservice.exception;

/**
 * Custom exception class for handling product return related exceptions.
 * This class extends the base Exception class and provides a constructor
 * to initialize the exception with a custom error message.
 */
public class ProductReturnException extends Exception {

    /**
     * Constructs a new ProductReturnException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     */
    public ProductReturnException(String message) {
        super(message);
    }

}