package com.bit.apigateway.exception;

/**
 * Custom exception class to represent a situation when the report service is unavailable.
 * This exception is thrown when a request to the report service cannot be fulfilled due to
 * temporary unavailability or other reasons.
 */
public class ReportServiceUnavailableException extends RuntimeException {

    /**
     * Constructs a new ReportServiceUnavailableException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public ReportServiceUnavailableException(String message) {
        super(message);
    }
}