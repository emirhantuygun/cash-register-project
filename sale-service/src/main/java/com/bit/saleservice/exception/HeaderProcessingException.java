package com.bit.saleservice.exception;

/**
 * Custom exception class for handling issues related to header processing in the Sale Service.
 * This exception is thrown when there is an error during the processing of request headers.
 */
public class HeaderProcessingException extends Exception {

    /**
     * Constructs a new HeaderProcessingException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public HeaderProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs a new HeaderProcessingException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method)
     */
    public HeaderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}