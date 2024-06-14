package com.bit.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class _GlobalExceptionHandler {

    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<String> handleMissingAuthorizationHeaderException(MissingAuthorizationHeaderException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidAuthorizationHeaderException.class)
    public ResponseEntity<String> handleInvalidAuthorizationHeaderException(InvalidAuthorizationHeaderException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<String> handleTokenNotFoundException(TokenNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
