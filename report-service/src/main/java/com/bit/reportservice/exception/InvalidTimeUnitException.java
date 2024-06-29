package com.bit.reportservice.exception;

/**
 * This class represents an exception that is thrown when an invalid time unit is encountered.
 * It extends the RuntimeException class, which means it does not require explicit catching.
 */
public class InvalidTimeUnitException extends RuntimeException  {

    /**
     * Constructs a new InvalidTimeUnitException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public InvalidTimeUnitException(String message) {
        super(message);
    }
}
