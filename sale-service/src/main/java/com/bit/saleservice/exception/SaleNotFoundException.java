package com.bit.saleservice.exception;

/**
 * This class represents a custom exception that is thrown when a sale is not found.
 * It extends the RuntimeException class, which means it does not require explicit
 * catching or declaration in the method signature.
 */
public class SaleNotFoundException extends RuntimeException {

    /**
     * Constructs a new SaleNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     */
    public SaleNotFoundException(String message) {
        super(message);
    }
}
