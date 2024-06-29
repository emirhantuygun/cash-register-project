package com.bit.reportservice.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * This class is a global exception handler for the report service.
 * It uses Spring's ControllerAdvice to handle exceptions thrown by the controllers.
 * The class is annotated with Log4j2 for logging purposes.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@ControllerAdvice
public class _GlobalExceptionHandler {

    /**
     * This method handles SaleServiceException.
     * It logs the exception message and returns a ResponseEntity with a 500 status code and the exception message as the body.
     *
     * @param ex The SaleServiceException to be handled.
     * @return A ResponseEntity with a 500 status code and the exception message as the body.
     */
    @ExceptionHandler(SaleServiceException.class)
    public ResponseEntity<String> handleSaleServiceException(SaleServiceException ex) {
        log.error("Handling SaleServiceException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * This method handles HeaderProcessingException.
     * It logs the exception message and returns a ResponseEntity with a 500 status code and the exception message as the body.
     *
     * @param ex The HeaderProcessingException to be handled.
     * @return A ResponseEntity with a 500 status code and the exception message as the body.
     */
    @ExceptionHandler(HeaderProcessingException.class)
    public ResponseEntity<String> handleHeaderProcessingException(HeaderProcessingException ex) {
        log.error("Handling HeaderProcessingException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This method handles ReceiptGenerationException.
     * It logs the exception message and returns a ResponseEntity with a 500 status code and the exception message as the body.
     *
     * @param ex The ReceiptGenerationException to be handled.
     * @return A ResponseEntity with a 500 status code and the exception message as the body.
     */
    @ExceptionHandler(ReceiptGenerationException.class)
    public ResponseEntity<String> handleReceiptGenerationException(ReceiptGenerationException ex) {
        log.error("Handling ReceiptGenerationException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This method handles ChartGenerationException.
     * It logs the exception message and returns a ResponseEntity with a 500 status code and the exception message as the body.
     *
     * @param ex The ChartGenerationException to be handled. This exception is thrown when there is an issue with generating charts.
     * @return A ResponseEntity with a 500 status code and the exception message as the body. This response is sent back to the client.
     */
    @ExceptionHandler(ChartGenerationException.class)
    public ResponseEntity<String> handleChartGenerationException(ChartGenerationException ex) {
        log.error("Handling ChartGenerationException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This method handles InvalidTimeUnitException.
     * It logs the exception message and returns a ResponseEntity with a 400 status code and the exception message as the body.
     *
     * @param ex The InvalidTimeUnitException to be handled. This exception is thrown when an invalid time unit is provided.
     * @return A ResponseEntity with a 400 status code and the exception message as the body. This response is sent back to the client.
     */
    @ExceptionHandler(InvalidTimeUnitException.class)
    public ResponseEntity<String> handleInvalidTimeUnitException(InvalidTimeUnitException ex) {
        log.error("Handling InvalidTimeUnitException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
