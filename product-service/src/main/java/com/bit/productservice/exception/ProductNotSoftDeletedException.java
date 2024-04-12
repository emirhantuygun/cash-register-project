package com.bit.productservice.exception;

public class ProductNotSoftDeletedException extends RuntimeException {

    public ProductNotSoftDeletedException(String message) {
        super(message);
    }

}
