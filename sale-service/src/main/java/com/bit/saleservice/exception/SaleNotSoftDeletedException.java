package com.bit.saleservice.exception;

/**
 * This exception is thrown when a sale that is not soft-deleted is attempted to be deleted.
 * It extends RuntimeException to allow for unchecked exceptions.
 */
public class SaleNotSoftDeletedException extends RuntimeException {

    /**
     * Constructs a new SaleNotSoftDeletedException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public SaleNotSoftDeletedException(String message) {
        super(message);
    }
}
