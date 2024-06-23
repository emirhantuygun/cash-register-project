package com.bit.saleservice.exception;

/**
 * Custom exception class for parsing errors.
 * This exception is thrown when there is an issue with parsing data.
 */
public class ParsingException extends RuntimeException {

    /**
     * Constructs a new ParsingException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *            (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
