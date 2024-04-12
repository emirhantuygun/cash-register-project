package com.bit.productservice.exception;

import com.bit.productservice.ProductServiceApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.FieldError;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(ProductServiceApplication.class);

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex) {
        logger.error("Product not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotSoftDeletedException.class)
    @ResponseBody
    public ResponseEntity<String> handleProductNotSoftDeletedException(ProductNotSoftDeletedException ex) {
        logger.error("Product not soft-deleted: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("Failed to read HTTP message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        String errorMessage = "Validation errors: " + errors;
        logger.error(errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}
