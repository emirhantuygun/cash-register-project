package com.bit.saleservice.exception;

public class InvalidPaymentMethodException extends RuntimeException {
    public InvalidPaymentMethodException(String message) {
        super(message);
    }

    public InvalidPaymentMethodException(String message, Throwable cause) {
        super(message, cause);
    }

}
