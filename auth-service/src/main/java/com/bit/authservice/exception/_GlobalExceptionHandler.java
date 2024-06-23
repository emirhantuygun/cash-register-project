package com.bit.authservice.exception;

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
 * It catches various exceptions and returns appropriate HTTP responses with meaningful error messages.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@ControllerAdvice
public class _GlobalExceptionHandler {

    /**
     * This method handles HttpMessageNotReadableException.
     * It logs the exception message and returns a ResponseEntity with a status of BAD_REQUEST and a body message of "Invalid request body."
     *
     * @param ex The HttpMessageNotReadableException that was caught.
     * @return A ResponseEntity with a status of BAD_REQUEST and a body message of "Invalid request body."
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException caught: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body.");
    }

    /**
     * This method handles MethodArgumentNotValidException.
     * It catches the exception when a request body does not meet validation requirements.
     *
     * @param ex The MethodArgumentNotValidException that was caught.
     * @return A ResponseEntity with a status of BAD_REQUEST and a body message containing the validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        String errorMessage = "Validation errors: " + errors;
        log.error("MethodArgumentNotValidException caught: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * This method handles MissingAuthorizationHeaderException.
     * It catches the exception when the authorization header is missing in the request.
     *
     * @param ex The MissingAuthorizationHeaderException that was caught.
     * @return A ResponseEntity with a status of BAD_REQUEST and a body message containing the exception message.
     */
    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<String> handleMissingAuthorizationHeaderException(MissingAuthorizationHeaderException ex) {
        log.error("MissingAuthorizationHeaderException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * This method handles InvalidAuthorizationHeaderException.
     * It catches the exception when the authorization header in the request is invalid.
     *
     * @param ex The InvalidAuthorizationHeaderException that was caught.
     * @return A ResponseEntity with a status of BAD_REQUEST and a body message containing the exception message.
     */
    @ExceptionHandler(InvalidAuthorizationHeaderException.class)
    public ResponseEntity<String> handleInvalidAuthorizationHeaderException(InvalidAuthorizationHeaderException ex) {
        log.error("InvalidAuthorizationHeaderException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * This method handles TokenNotFoundException.
     * It catches the exception when a token is not found in the system.
     *
     * @param ex The TokenNotFoundException that was caught.
     * @return A ResponseEntity with a status of NOT_FOUND and a body message containing the exception message.
     */
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<String> handleTokenNotFoundException(TokenNotFoundException ex) {
        log.error("TokenNotFoundException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * This method handles AuthenticationFailedException.
     * It catches the exception when the authentication process fails.
     *
     * @param ex The AuthenticationFailedException that was caught.
     * @return A ResponseEntity with a status of UNAUTHORIZED and a body message containing the exception message.
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<String> handleAuthenticationFailedException(AuthenticationFailedException ex) {
        log.error("AuthenticationFailedException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * This method handles UserNotFoundException.
     * It catches the exception when a user is not found in the system.
     *
     * @param ex The UserNotFoundException that was caught.
     * @return A ResponseEntity with a status of NOT_FOUND and a body message containing the exception message.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("UserNotFoundException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * This method handles InvalidRefreshTokenException.
     * It catches the exception when the refresh token in the request is invalid.
     *
     * @param ex The InvalidRefreshTokenException that was caught.
     * @return A ResponseEntity with a status of BAD_REQUEST and a body message containing the exception message.
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<String> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
        log.error("InvalidRefreshTokenException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * This method handles UsernameExtractionException.
     * It catches the exception when the username cannot be extracted from the request.
     *
     * @param ex The UsernameExtractionException that was caught.
     * @return A ResponseEntity with a status of INTERNAL_SERVER_ERROR and a body message containing the exception message.
     */
    @ExceptionHandler(UsernameExtractionException.class)
    public ResponseEntity<String> handleUsernameExtractionException(UsernameExtractionException ex) {
        log.error("UsernameExtractionException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This method handles RedisOperationException.
     * It catches the exception when an operation related to Redis fails.
     *
     * @param ex The RedisOperationException that was caught.
     * @return A ResponseEntity with a status of INTERNAL_SERVER_ERROR and a body message containing the exception message.
     */
    @ExceptionHandler(RedisOperationException.class)
    public ResponseEntity<String> handleRedisOperationException(RedisOperationException ex) {
        log.error("RedisOperationException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This method handles RoleNotFoundException.
     * It catches the exception when a role is not found in the system.
     *
     * @param ex The RoleNotFoundException that was caught.
     * @return A ResponseEntity with a status of NOT_FOUND and a body message containing the exception message.
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<String> handleRoleNotFoundException(RoleNotFoundException ex) {
        log.error("RoleNotFoundException caught: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
