package com.bit.saleservice.exception;

public class PaymentMethodUpdateNotAllowedException extends RuntimeException {
    public PaymentMethodUpdateNotAllowedException(String message) {
        super(message);
    }
}
