package com.bit.saleservice.exception;

public class InvalidMixedPaymentException extends RuntimeException {
    public InvalidMixedPaymentException(String message) {
        super(message);
    }
}
