package com.bit.usermanagementservice.exception;

public class RabbitMQException extends RuntimeException {
    public RabbitMQException(String message) {
        super(message);
    }

    public RabbitMQException(String message, Throwable cause) {
        super(message, cause);
    }
}