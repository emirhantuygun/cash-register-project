package com.bit.apigateway.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This class is a global exception handler for the API Gateway.
 * It catches and handles various exceptions that may occur during the API Gateway's operation.
 * The exceptions are logged using Log4j2 and appropriate HTTP responses are returned to the client.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@RestControllerAdvice
public class _GlobalExceptionHandler {

    /**
     * This method handles the TokenNotFoundException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 401 (UNAUTHORIZED)
     * and the error message as the response body.
     *
     * @param ex The TokenNotFoundException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(TokenNotFoundException ex) {
        log.error("TokenNotFoundException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * This method handles the InvalidTokenException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 401 (UNAUTHORIZED)
     * and the error message as the response body.
     *
     * @param ex The InvalidTokenException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
        log.error("InvalidTokenException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * This method handles the LoggedOutTokenException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 401 (UNAUTHORIZED)
     * and the error message as the response body.
     *
     * @param ex The LoggedOutTokenException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(LoggedOutTokenException.class)
    public ResponseEntity<String> handleLoggedOutTokenException(LoggedOutTokenException ex) {
        log.error("LoggedOutTokenException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * This method handles the MissingRolesException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 401 (UNAUTHORIZED)
     * and the error message as the response body.
     *
     * @param ex The MissingRolesException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(MissingRolesException.class)
    public ResponseEntity<String> handleMissingRolesException(MissingRolesException ex) {
        log.error("MissingRolesException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * This method handles the InsufficientRolesException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 403 (FORBIDDEN)
     * and the error message as the response body.
     *
     * @param ex The InsufficientRolesException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(InsufficientRolesException.class)
    public ResponseEntity<String> handleInsufficientRolesException(InsufficientRolesException ex) {
        log.error("InsufficientRolesException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * This method handles the MissingAuthorizationHeaderException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 401 (UNAUTHORIZED)
     * and the error message as the response body.
     *
     * @param ex The MissingAuthorizationHeaderException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<String> handleMissingAuthorizationHeaderException(MissingAuthorizationHeaderException ex) {
        log.error("MissingAuthorizationHeaderException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * This method handles the AuthServiceUnavailableException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 503 (SERVICE_UNAVAILABLE)
     * and the error message as the response body.
     *
     * @param ex The AuthServiceUnavailableException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(AuthServiceUnavailableException.class)
    public ResponseEntity<String> handleAuthServiceUnavailableException(AuthServiceUnavailableException ex) {
        log.error("AuthServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    /**
     * This method handles the UserServiceUnavailableException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 503 (SERVICE_UNAVAILABLE)
     * and the error message as the response body.
     *
     * @param ex The UserServiceUnavailableException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<String> handleUserServiceUnavailableException(UserServiceUnavailableException ex) {
        log.error("UserServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    /**
     * This method handles the ProductServiceUnavailableException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 503 (SERVICE_UNAVAILABLE)
     * and the error message as the response body.
     *
     * @param ex The ProductServiceUnavailableException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(ProductServiceUnavailableException.class)
    public ResponseEntity<String> handleProductServiceUnavailableException(ProductServiceUnavailableException ex) {
        log.error("ProductServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    /**
     * This method handles the SaleServiceUnavailableException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 503 (SERVICE_UNAVAILABLE)
     * and the error message as the response body.
     *
     * @param ex The SaleServiceUnavailableException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(SaleServiceUnavailableException.class)
    public ResponseEntity<String> handleSaleServiceUnavailableException(SaleServiceUnavailableException ex) {
        log.error("SaleServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }

    /**
     * This method handles the ReportServiceUnavailableException.
     * It logs the error message and returns a ResponseEntity with an HTTP status code of 503 (SERVICE_UNAVAILABLE)
     * and the error message as the response body.
     *
     * @param ex The ReportServiceUnavailableException that was caught.
     * @return A ResponseEntity with the appropriate HTTP status code and error message.
     */
    @ExceptionHandler(ReportServiceUnavailableException.class)
    public ResponseEntity<String> handleReportServiceUnavailableException(ReportServiceUnavailableException ex) {
        log.error("ReportServiceUnavailableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
    }
}
