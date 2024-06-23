package com.bit.saleservice.exception;

/**
 * This exception is thrown when a product is requested to be sold, but there is not enough stock available.
 */
public class ProductOutOfStockException extends RuntimeException {

    /**
     * Constructs a new ProductOutOfStockException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method)
     */
    public ProductOutOfStockException(String message) {
        super(message);
    }
}
