package com.bit.saleservice.exception;

/**
 * Custom exception class for handling RabbitMQ related exceptions.
 * This class extends the RuntimeException class to allow unchecked exceptions.
 */
public class RabbitMQException extends RuntimeException {

    /**
     * Constructs a new RabbitMQException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public RabbitMQException(String message, Throwable cause) {
        super(message, cause);
    }
}