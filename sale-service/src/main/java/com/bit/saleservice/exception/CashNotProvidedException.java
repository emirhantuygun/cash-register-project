package com.bit.saleservice.exception;


public class CashNotProvidedException extends RuntimeException {

    public CashNotProvidedException(String message) {
        super(message);
    }
}