package com.bit.saleservice.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * This class is a global exception handler for the sale service.
 * It extends ResponseEntityExceptionHandler to handle exceptions and provide appropriate responses.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@ControllerAdvice
public class _GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles the CampaignNotApplicableException.
     *
     * @param ex the CampaignNotApplicableException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(CampaignNotApplicableException.class)
    public ResponseEntity<Object> handleCampaignNotApplicableException(CampaignNotApplicableException ex) {
        log.error("Campaign not applicable exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

    }

    /**
     * Handles the SaleNotSoftDeletedException.
     *
     * @param ex the SaleNotSoftDeletedException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(SaleNotSoftDeletedException.class)
    public ResponseEntity<Object> handleSaleNotSoftDeletedException(SaleNotSoftDeletedException ex) {
        log.error("Sale not soft deleted exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the PaymentMethodUpdateNotAllowedException.
     *
     * @param ex the PaymentMethodUpdateNotAllowedException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(PaymentMethodUpdateNotAllowedException.class)
    public ResponseEntity<Object> handlePaymentMethodUpdateNotAllowedException(PaymentMethodUpdateNotAllowedException ex) {
        log.error("Payment method update not allowed exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the MixedPaymentNotFoundException.
     *
     * @param ex the MixedPaymentNotFoundException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(MixedPaymentNotFoundException.class)
    public ResponseEntity<Object> handleMixedPaymentNotFoundException(MixedPaymentNotFoundException ex) {
        log.error("Mixed payment not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the InvalidMixedPaymentException.
     *
     * @param ex the InvalidMixedPaymentException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(InvalidMixedPaymentException.class)
    public ResponseEntity<Object> handleInvalidMixedPaymentException(InvalidMixedPaymentException ex) {
        log.error("Invalid mixed payment exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the InvalidPaymentMethodException.
     *
     * @param ex the InvalidPaymentMethodException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(InvalidPaymentMethodException.class)
    public ResponseEntity<Object> handleInvalidPaymentMethodException(InvalidPaymentMethodException ex) {
        log.error("Invalid payment method exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the CashNotProvidedException.
     *
     * @param ex the CashNotProvidedException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(CashNotProvidedException.class)
    public ResponseEntity<Object> handleCashNotProvidedException(CashNotProvidedException ex) {
        log.error("Cash not provided exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the CampaignNotFoundException.
     *
     * @param ex the CampaignNotFoundException to handle
     * @return a ResponseEntity with a NOT_FOUND status and the exception message as the body
     */
    @ExceptionHandler(CampaignNotFoundException.class)
    public ResponseEntity<Object> handleCampaignNotFoundException(CampaignNotFoundException ex) {
        log.error("Campaign not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles the DuplicateCampaignException.
     *
     * @param ex the DuplicateCampaignException to handle
     * @return a ResponseEntity with a CONFLICT status and the exception message as the body
     */
    @ExceptionHandler(DuplicateCampaignException.class)
    public ResponseEntity<Object> handleDuplicateCampaignException(DuplicateCampaignException ex) {
        log.error("Duplicate campaign exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Handles the InsufficientCashException.
     *
     * @param ex the InsufficientCashException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(InsufficientCashException.class)
    public ResponseEntity<Object> handleInsufficientCashException(InsufficientCashException ex) {
        log.error("Insufficient cash exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the InsufficientMixedPaymentException.
     *
     * @param ex the InsufficientMixedPaymentException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(InsufficientMixedPaymentException.class)
    public ResponseEntity<Object> handleInsufficientMixedPaymentException(InsufficientMixedPaymentException ex) {
        log.error("Insufficient mixed payment exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the ProductNotFoundException.
     *
     * @param ex the ProductNotFoundException to handle
     * @return a ResponseEntity with a NOT_FOUND status and the exception message as the body
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException ex) {
        log.error("Product not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles the ProductOutOfStockException.
     *
     * @param ex the ProductOutOfStockException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<String> handleProductOutOfStockException(ProductOutOfStockException ex) {
        log.error("Product out of stock exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles the ServerErrorException.
     *
     * @param ex the ServerErrorException to handle
     * @return a ResponseEntity with an INTERNAL_SERVER_ERROR status and the exception message as the body
     */
    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<String> handleServerErrorException(ServerErrorException ex) {
        log.error("Server error exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * Handles the SaleNotFoundException.
     *
     * @param ex the SaleNotFoundException to handle
     * @return a ResponseEntity with a NOT_FOUND status and the exception message as the body
     */
    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<String> handleSaleNotFoundException(SaleNotFoundException ex) {
        log.error("Sale not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles the ProductServiceException.
     *
     * @param ex the ProductServiceException to handle
     * @return a ResponseEntity with a SERVICE_UNAVAILABLE status and the exception message as the body
     */
    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<String> handleProductServiceException(ProductServiceException ex) {
        log.error("Product service exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    /**
     * Handles the HeaderProcessingException.
     *
     * @param ex the HeaderProcessingException to handle
     * @return a ResponseEntity with an INTERNAL_SERVER_ERROR status and the exception message as the body
     */
    @ExceptionHandler(HeaderProcessingException.class)
    public ResponseEntity<String> handleHeaderProcessingException(HeaderProcessingException ex) {
        log.error("Header processing exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles the ProductReturnException.
     *
     * @param ex the ProductReturnException to handle
     * @return a ResponseEntity with an INTERNAL_SERVER_ERROR status and the exception message as the body
     */
    @ExceptionHandler(ProductReturnException.class)
    public ResponseEntity<String> handleProductReturnException(ProductReturnException ex) {
        log.error("Product return exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles RabbitMQException.
     *
     * @param ex the RabbitMQException to handle
     * @return a ResponseEntity with an INTERNAL_SERVER_ERROR status and the exception message as the body
     */
    @ExceptionHandler(RabbitMQException.class)
    public ResponseEntity<String> handleRabbitMQException(RabbitMQException ex) {
        log.error("RabbitMQ exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles ParsingException.
     *
     * @param ex the ParsingException to handle
     * @return a ResponseEntity with a BAD_REQUEST status and the exception message as the body
     */
    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<String> handleParsingException(ParsingException ex) {
        log.error("Parsing exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
