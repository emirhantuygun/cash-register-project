package com.bit.productservice.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * This class serves as a global exception handler for the application.
 * It catches and handles specific exceptions that may occur during the execution of the application.
 * The exceptions are logged using Log4j2 and appropriate HTTP responses are returned to the client.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@ControllerAdvice
public class _GlobalExceptionHandler {

    /**
     * Handles HttpMessageNotReadableException.
     * This exception is thrown when the request body cannot be read due to invalid format or missing required fields.
     *
     * @param ex The HttpMessageNotReadableException that occurred.
     * @return A ResponseEntity with a status of 400 (Bad Request) and a message indicating the error.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body.");
    }

    /**
     * Handles MethodArgumentNotValidException.
     * This exception is thrown when a method argument fails validation.
     *
     * @param ex The MethodArgumentNotValidException that occurred.
     * @return A ResponseEntity with a status of 400 (Bad Request) and a message indicating the validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException occurred: {}", ex.getMessage(), ex);

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        String errorMessage = "Validation errors: " + errors;
        log.error(errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * Handles AlgorithmNotFoundException.
     * This method catches and handles the custom exception AlgorithmNotFoundException.
     * When this exception is thrown, it logs the error message and returns a ResponseEntity with a status of 500 (Internal Server Error)
     * and the exception message as the response body.
     *
     * @param ex The AlgorithmNotFoundException that occurred.
     * @return A ResponseEntity with a status of 500 (Internal Server Error) and the exception message as the response body.
     */
    @ExceptionHandler(AlgorithmNotFoundException.class)
    public ResponseEntity<String> handleAlgorithmNotFoundException(AlgorithmNotFoundException ex) {
        log.error("AlgorithmNotFoundException occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles ProductNotFoundException.
     * This method catches and handles the custom exception ProductNotFoundException.
     * When this exception is thrown, it logs the error message and returns a ResponseEntity with a status of 404 (Not Found)
     * and the exception message as the response body.
     *
     * @param ex The ProductNotFoundException that occurred.
     * @return A ResponseEntity with a status of 404 (Not Found) and the exception message as the response body.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex) {
        log.error("ProductNotFoundException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles ProductNotSoftDeletedException.
     * This method catches and handles the custom exception ProductNotSoftDeletedException.
     * When this exception is thrown, it logs the error message and returns a ResponseEntity with a status of 400 (Bad Request)
     * and the exception message as the response body.
     *
     * @param ex The ProductNotSoftDeletedException that occurred.
     * @return A ResponseEntity with a status of 400 (Bad Request) and the exception message as the response body.
     */
    @ExceptionHandler(ProductNotSoftDeletedException.class)
    @ResponseBody
    public ResponseEntity<String> handleProductNotSoftDeletedException(ProductNotSoftDeletedException ex) {
        log.error("ProductNotSoftDeletedException occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}