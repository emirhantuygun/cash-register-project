package com.bit.saleservice.exception;

public class ProductStockCheckException extends RuntimeException {
    public ProductStockCheckException(String message) {
        super(message);
    }

    public ProductStockCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
