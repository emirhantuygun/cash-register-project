package com.bit.saleservice.exception;

public class SaleNotSoftDeletedException extends RuntimeException {
    public SaleNotSoftDeletedException(String message) {
        super(message);
    }
}
