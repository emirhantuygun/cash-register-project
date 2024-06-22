package com.bit.apigateway.exception;

/**
 * Custom exception class to represent a situation when the report service is unavailable.
 * This exception is thrown when a request to the report service cannot be fulfilled due to
 * temporary unavailability or other reasons.
 */
public class ReportServiceUnavailableException extends RuntimeException {
    public ReportServiceUnavailableException(String message) {
        super(message);
    }
}