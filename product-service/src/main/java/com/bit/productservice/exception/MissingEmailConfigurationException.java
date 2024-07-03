package com.bit.productservice.exception;

/**
 * This class represents a custom exception that is thrown when the email configuration is missing.
 * It extends the EmailException class and provides a constructor to initialize the exception with a message.
 */
public class MissingEmailConfigurationException extends EmailException {

    /**
     * Constructs a new MissingEmailConfigurationException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public MissingEmailConfigurationException(String message) {
        super(message);
    }
}