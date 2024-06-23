package com.bit.productservice.exception;

/**
 * Custom exception class to be thrown when an algorithm is not found.
 * This exception is typically thrown when a specific algorithm is required
 * but is not available in the system.
 */
public class AlgorithmNotFoundException extends Exception {

    /**
     * Constructs a new AlgorithmNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *            (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AlgorithmNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}