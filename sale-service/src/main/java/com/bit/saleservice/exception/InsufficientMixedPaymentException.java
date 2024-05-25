package com.bit.saleservice.exception;

public class InsufficientMixedPaymentException extends RuntimeException {
    public InsufficientMixedPaymentException(String message) {
        super(message);
    }
}
