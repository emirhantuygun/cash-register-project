package com.bit.saleservice.exception;

/**
 * Custom exception class to handle insufficient cash during sale operations.
 * This exception is thrown when the cash provided for a sale operation is less than the required amount.
 */
public class InsufficientCashException extends RuntimeException {

    /**
     * Constructs a new instance of {@code InsufficientCashException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InsufficientCashException(String message) {
        super(message);
    }
}
