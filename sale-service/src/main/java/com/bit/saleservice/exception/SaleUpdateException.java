package com.bit.saleservice.exception;

public class SaleUpdateException extends RuntimeException {
    public SaleUpdateException(String message) {
        super(message);
    }

    public SaleUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}