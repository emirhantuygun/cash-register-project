package com.bit.reportservice.exception;

/**
 * Custom exception class for handling exceptions related to sales service.
 * This class extends RuntimeException, which means it does not require explicit catching.
 */
public class SaleServiceException extends RuntimeException  {

    /**
     * Constructs a new SaleServiceException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public SaleServiceException(String message) {
        super(message);
    }
}
