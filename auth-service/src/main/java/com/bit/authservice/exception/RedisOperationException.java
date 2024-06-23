package com.bit.authservice.exception;

/**
 * Custom exception class for Redis operation failures.
 * This class extends the base Exception class and provides a constructor
 * to initialize the exception with a custom error message and the cause of the exception.
 */
public class RedisOperationException extends Exception {

    /**
     * Constructs a new RedisOperationException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}