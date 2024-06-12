package com.bit.apigateway.exception;

public class ReportServiceUnavailableException extends RuntimeException {
    public ReportServiceUnavailableException(String message) {
        super(message);
    }
}