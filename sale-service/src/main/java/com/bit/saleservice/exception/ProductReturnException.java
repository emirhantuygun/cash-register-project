package com.bit.saleservice.exception;

public class ProductReturnException extends Exception {
    public ProductReturnException(String message) {
        super(message);
    }

    public ProductReturnException(String message, Throwable cause) {
        super(message, cause);
    }
}