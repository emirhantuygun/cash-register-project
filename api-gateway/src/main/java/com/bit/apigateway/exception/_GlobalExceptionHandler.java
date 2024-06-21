package com.bit.apigateway.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class _GlobalExceptionHandler {

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(TokenNotFoundException ex) {
        log.error("TokenNotFoundException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
        log.error("InvalidTokenException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LoggedOutTokenException.class)
    public ResponseEntity<String> handleLoggedOutTokenException(LoggedOutTokenException ex) {
        log.error("LoggedOutTokenException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MissingRolesException.class)
    public ResponseEntity<String> handleMissingRolesException(MissingRolesException ex) {
        log.error("MissingRolesException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InsufficientRolesException.class)
    public ResponseEntity<String> handleInsufficientRolesException(InsufficientRolesException ex) {
        log.error("InsufficientRolesException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<String> handleMissingAuthorizationHeaderException(MissingAuthorizationHeaderException ex) {
        log.error("MissingAuthorizationHeaderException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthServiceUnavailableException.class)
    public ResponseEntity<String> handleAuthServiceUnavailableException(AuthServiceUnavailableException ex) {
        log.error("AuthServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<String> handleUserServiceUnavailableException(UserServiceUnavailableException ex) {
        log.error("UserServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ProductServiceUnavailableException.class)
    public ResponseEntity<String> handleProductServiceUnavailableException(ProductServiceUnavailableException ex) {
        log.error("ProductServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(SaleServiceUnavailableException.class)
    public ResponseEntity<String> handleSaleServiceUnavailableException(SaleServiceUnavailableException ex) {
        log.error("SaleServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ReportServiceUnavailableException.class)
    public ResponseEntity<String> handleReportServiceUnavailableException(ReportServiceUnavailableException ex) {
        log.error("ReportServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }
}
