package com.bit.saleservice.exception;

/**
 * This class represents an exception that is thrown when a mixed payment type is invalid.
 * It extends the RuntimeException class, which means it does not require explicit catching.
 */
public class InvalidMixedPaymentException extends RuntimeException {

    /**
     * Constructs a new InvalidMixedPaymentException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public InvalidMixedPaymentException(String message) {
        super(message);
    }
}
