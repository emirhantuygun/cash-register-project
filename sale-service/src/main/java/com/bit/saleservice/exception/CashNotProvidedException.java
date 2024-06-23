package com.bit.saleservice.exception;

/**
 * Custom exception class to handle the scenario when cash is not provided.
 * This exception is thrown when the cash amount is required but not provided.
 */
public class CashNotProvidedException extends RuntimeException {

    /**
     * Constructs a new CashNotProvidedException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public CashNotProvidedException(String message) {
        super(message);
    }
}