package com.bit.productservice.exception;

/**
 * This class represents an exception that is thrown when an email address does not meet the required format.
 * It extends the EmailException class.
 */
public class InvalidEmailFormatException extends EmailException {

    /**
     * Constructs a new InvalidEmailFormatException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}