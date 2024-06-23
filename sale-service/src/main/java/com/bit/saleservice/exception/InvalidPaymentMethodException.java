package com.bit.saleservice.exception;

/**
 * This class represents an exception that is thrown when an invalid payment method is used.
 * It extends RuntimeException, which means it does not require explicit catching.
 */
public class InvalidPaymentMethodException extends RuntimeException {

    /**
     * Constructs a new InvalidPaymentMethodException with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *                Throwable.getMessage() method.
     */
    public InvalidPaymentMethodException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPaymentMethodException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                Throwable.getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the Throwable.getCause() method).
     *                (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public InvalidPaymentMethodException(String message, Throwable cause) {
        super(message, cause);
    }

}
