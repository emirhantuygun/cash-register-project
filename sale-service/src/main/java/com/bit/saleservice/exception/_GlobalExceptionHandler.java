package com.bit.saleservice.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@ControllerAdvice
public class _GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CampaignNotApplicableException.class)
    public ResponseEntity<Object> handleCampaignNotApplicableException(CampaignNotApplicableException ex) {
        log.error("Campaign not applicable exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

    }
    @ExceptionHandler(SaleNotSoftDeletedException.class)
    public ResponseEntity<Object> handleSaleNotSoftDeletedException(SaleNotSoftDeletedException ex) {
        log.error("Sale not soft deleted exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(PaymentMethodUpdateNotAllowedException.class)
    public ResponseEntity<Object> handlePaymentMethodUpdateNotAllowedException(PaymentMethodUpdateNotAllowedException ex) {
        log.error("Payment method update not allowed exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(MixedPaymentNotFoundException.class)
    public ResponseEntity<Object> handleMixedPaymentNotFoundException(MixedPaymentNotFoundException ex) {
        log.error("Mixed payment not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(InvalidMixedPaymentException.class)
    public ResponseEntity<Object> handleInvalidMixedPaymentException(InvalidMixedPaymentException ex) {
        log.error("Invalid mixed payment exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<Object> handleInvalidPaymentMethodException(InvalidPaymentMethodException ex) {
        log.error("Invalid payment method exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(CashNotProvidedException.class)
    public ResponseEntity<Object> handleCashNotProvidedException(CashNotProvidedException ex) {
        log.error("Cash not provided exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CampaignNotFoundException.class)
    public ResponseEntity<Object> handleCampaignNotFoundException(CampaignNotFoundException ex) {
        log.error("Campaign not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateCampaignException.class)
    public ResponseEntity<Object> handleDuplicateCampaignException(DuplicateCampaignException ex) {
        log.error("Duplicate campaign exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientCashException.class)
    public ResponseEntity<Object> handleInsufficientCashException(InsufficientCashException ex) {
        log.error("Insufficient cash exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientMixedPaymentException.class)
    public ResponseEntity<Object> handleInsufficientMixedPaymentException(InsufficientMixedPaymentException ex) {
        log.error("Insufficient mixed payment exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex) {
        log.error("Product not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<String> handleProductOutOfStockException(ProductOutOfStockException ex) {
        log.error("Product out of stock exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<String> handleServerErrorException(ServerErrorException ex) {
        log.error("Server error exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<String> handleSaleNotFoundException(SaleNotFoundException ex) {
        log.error("Sale not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<String> handleProductServiceException(ProductServiceException ex) {
        log.error("Product service exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(HeaderProcessingException.class)
    public ResponseEntity<String> handleHeaderProcessingException(HeaderProcessingException ex) {
        log.error("Header processing exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProductReturnException.class)
    public ResponseEntity<String> handleProductReturnException(ProductReturnException ex) {
        log.error("Product return exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RabbitMQException.class)
    public ResponseEntity<String> handleRabbitMQException(RabbitMQException ex) {
        log.error("RabbitMQ exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
