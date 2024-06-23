package com.bit.saleservice.exception;

/**
 * Custom exception class for payment method update not allowed scenarios.
 * This exception is thrown when an attempt is made to update a payment method
 * that is not allowed, such as when the payment method is associated with
 * existing orders or transactions.
 */
public class PaymentMethodUpdateNotAllowedException extends RuntimeException {

    /**
     * Constructs a new PaymentMethodUpdateNotAllowedException with the specified
     * detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     */
    public PaymentMethodUpdateNotAllowedException(String message) {
        super(message);
    }
}
