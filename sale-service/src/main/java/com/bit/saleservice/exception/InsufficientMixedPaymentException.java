package com.bit.saleservice.exception;

/**
 * Custom exception class for handling insufficient mixed payment scenarios.
 * This exception is thrown when the total amount of mixed payment (cash and card)
 * is less than the required amount for a sale.
 */
public class InsufficientMixedPaymentException extends RuntimeException {

    /**
     * Constructs a new instance of {@code InsufficientMixedPaymentException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InsufficientMixedPaymentException(String message) {
        super(message);
    }
}
