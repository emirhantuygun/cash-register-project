package com.bit.saleservice.exception;

/**
 * Custom exception class for handling cases when a mixed payment (both cash and card) is not found.
 * This exception is thrown when a method is expected to return a mixed payment but none is found.
 */
public class MixedPaymentNotFoundException extends RuntimeException {

    /**
     * Constructs a new MixedPaymentNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public MixedPaymentNotFoundException(String message) {
        super(message);
    }
}