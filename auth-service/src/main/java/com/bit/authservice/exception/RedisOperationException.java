package com.bit.authservice.exception;

public class RedisOperationException extends Exception {
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}