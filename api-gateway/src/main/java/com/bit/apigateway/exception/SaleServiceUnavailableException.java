package com.bit.apigateway.exception;

/**
 * Custom exception class to handle situations when the Sale Service is unavailable.
 * This exception is thrown when a request to the Sale Service cannot be fulfilled due to
 * temporary unavailability or other reasons.
 */
public class SaleServiceUnavailableException extends RuntimeException {

    /**
     * Constructs a new SaleServiceUnavailableException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     */
    public SaleServiceUnavailableException(String message) {
        super(message);
    }
}