package com.bit.productservice.exception;

/**
 * This class represents an exception that is thrown when an email sending operation fails.
 * It extends the EmailException class and provides a constructor to initialize the exception with a custom message.
 */
public class EmailSendingFailedException extends EmailException {

    /**
     * Constructs a new EmailSendingFailedException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public EmailSendingFailedException(String message) {
        super(message);
    }
}