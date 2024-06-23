package com.bit.reportservice.exception;

/**
 * Custom exception class for handling receipt generation errors.
 * This class extends the base Exception class and provides constructors
 * to handle error messages with a cause.
 */
public class ReceiptGenerationException extends Exception {

    /**
     * Constructs a new ReceiptGenerationException with the specified detail message
     * and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method). (A null value is permitted,
     *                and indicates that the cause is nonexistent or unknown.)
     */
    public ReceiptGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}