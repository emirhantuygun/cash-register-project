package com.bit.reportservice.exception;

import com.bit.reportservice.ReportServiceApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class _GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LogManager.getLogger(ReportServiceApplication.class);

    @ExceptionHandler(SaleServiceException.class)
    public ResponseEntity<String> handleSaleServiceException(SaleServiceException ex) {
        logger.error("Sale service exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(HeaderProcessingException.class)
    public ResponseEntity<String> handleHeaderProcessingException(HeaderProcessingException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ReceiptGenerationException.class)
    public ResponseEntity<String> handleReceiptGenerationException(ReceiptGenerationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
