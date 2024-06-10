package com.bit.saleservice.exception;

import com.bit.saleservice.SaleServiceApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class _GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LogManager.getLogger(SaleServiceApplication.class);

    @ExceptionHandler(CampaignNotApplicableException.class)
    public ResponseEntity<Object> handleCampaignNotApplicableException(CampaignNotApplicableException ex) {
        logger.error("Campaign not applicable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

    }
    @ExceptionHandler(SaleNotSoftDeletedException.class)
    public ResponseEntity<Object> handleSaleNotSoftDeletedException(SaleNotSoftDeletedException ex) {
        logger.error("Sale not soft deleted: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(PaymentMethodUpdateNotAllowedException.class)
    public ResponseEntity<Object> handlePaymentMethodUpdateNotAllowedException(PaymentMethodUpdateNotAllowedException ex) {
        logger.error("Payment method update not allowed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(MixedPaymentNotFoundException.class)
    public ResponseEntity<Object> handleMixedPaymentNotFoundException(MixedPaymentNotFoundException ex) {
        logger.error("Mixed payment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(InvalidMixedPaymentException.class)
    public ResponseEntity<Object> handleInvalidMixedPaymentException(InvalidMixedPaymentException ex) {
        logger.error("Invalid mixed payment: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<Object> handleInvalidPaymentMethodException(InvalidPaymentMethodException ex) {
        logger.error("Invalid payment method: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(CashNotProvidedException.class)
    public ResponseEntity<Object> handleCashNotProvidedException(CashNotProvidedException ex) {
        logger.error("Cash not provided: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CampaignNotFoundException.class)
    public ResponseEntity<Object> handleCampaignNotFoundException(CampaignNotFoundException ex) {
        logger.error("Campaign not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateCampaignException.class)
    public ResponseEntity<Object> handleDuplicateCampaignException(DuplicateCampaignException ex) {
        logger.error("Duplicate campaign: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientCashException.class)
    public ResponseEntity<Object> handleInsufficientCashException(InsufficientCashException ex) {
        logger.error("Insufficient cash: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientMixedPaymentException.class)
    public ResponseEntity<Object> handleInsufficientMixedPaymentException(InsufficientMixedPaymentException ex) {
        logger.error("Insufficient mixed payment: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex) {
        logger.error("Product not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<String> handleClientErrorException(ClientErrorException ex) {
        logger.error("Client error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<String> handleServerErrorException(ServerErrorException ex) {
        logger.error("Server error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<String> handleSaleNotFoundException(SaleNotFoundException ex) {
        logger.error("Sale not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<String> handleProductServiceException(ProductServiceException ex) {
        logger.error("Product service exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
