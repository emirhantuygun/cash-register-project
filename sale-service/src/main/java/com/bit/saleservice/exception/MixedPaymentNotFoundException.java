package com.bit.saleservice.exception;


public class MixedPaymentNotFoundException extends RuntimeException {

    public MixedPaymentNotFoundException(String message) {
        super(message);
    }
}