package com.bit.usermanagementservice.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
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
 * This class is a global exception handler for the User Management Service.
 * It catches and handles various exceptions that may occur during the execution of the service.
 * The exceptions are logged using Log4j2 and appropriate HTTP responses are returned to the client.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@ControllerAdvice
public class _GlobalExceptionHandler {

    /**
     * This method handles the UserNotFoundException.
     * It logs the error message and returns a ResponseEntity with a NOT_FOUND status and the error message as the body.
     *
     * @param ex The UserNotFoundException that occurred.
     * @return A ResponseEntity with a NOT_FOUND status and the error message as the body.
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * This method handles the UserNotSoftDeletedException.
     * It logs the error message and returns a ResponseEntity with a BAD_REQUEST status and the error message as the body.
     *
     * @param ex The UserNotSoftDeletedException that occurred.
     * @return A ResponseEntity with a BAD_REQUEST status and the error message as the body.
     */
    @ExceptionHandler(UserNotSoftDeletedException.class)
    @ResponseBody
    public ResponseEntity<String> handleUserNotSoftDeletedException(UserNotSoftDeletedException ex) {
        log.error("User not soft-deleted: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * This method handles the AuthServiceException.
     * It logs the error message and returns a ResponseEntity with a INTERNAL_SERVER_ERROR status and the error message as the body.
     *
     * @param ex The AuthServiceException that occurred. This exception is thrown when there is a problem with the connection to the Auth service.
     * @return A ResponseEntity with a INTERNAL_SERVER_ERROR status and the error message as the body. This response indicates that the server encountered an unexpected condition that prevented it from fulfilling the request.
     */
    @ExceptionHandler(AuthServiceException.class)
    public ResponseEntity<String> handleAuthException(AuthServiceException ex) {
        log.error("Auth service connection failed: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * This method handles the InvalidRoleException.
     * It logs the error message and returns a ResponseEntity with a BAD_REQUEST status and the error message as the body.
     *
     * @param ex The InvalidRoleException that occurred. This exception is thrown when a user tries to assign an invalid role.
     * @return A ResponseEntity with a BAD_REQUEST status and the error message as the body. This response indicates that the request contains invalid parameters.
     */
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Object> handleInvalidRoleException(InvalidRoleException ex) {
        log.error("Invalid role: " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * This method handles IllegalArgumentException.
     * It logs the error message and returns a ResponseEntity with a BAD_REQUEST status and the error message as the body.
     *
     * @param ex The IllegalArgumentException that occurred. This exception is thrown when a method receives an argument that is not valid.
     * @return A ResponseEntity with a BAD_REQUEST status and the error message as the body. This response indicates that the request contains invalid parameters.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument exception: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * This method handles DataIntegrityViolationException.
     * It logs the error message and returns a ResponseEntity with a CONFLICT status and the error message as the body.
     *
     * @param ex The DataIntegrityViolationException that occurred. This exception is thrown when a database operation violates a constraint.
     * @return A ResponseEntity with a CONFLICT status and the error message as the body. This response indicates that the request could not be completed due to a conflict with the current state of the resource.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation exception: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Data integrity violation: " + ex.getMessage());
    }

    /**
     * This method handles HttpMessageNotReadableException.
     * It logs the error message and returns a ResponseEntity with a BAD_REQUEST status and a custom error message.
     *
     * @param ex The HttpMessageNotReadableException that occurred. This exception is thrown when the framework fails to read the HTTP message.
     * @return A ResponseEntity with a BAD_REQUEST status and a custom error message. This response indicates that the request body is invalid.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Failed to read HTTP message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body.");
    }

    /**
     * This method handles MethodArgumentNotValidException.
     * It catches the exception when a method argument fails validation.
     * It extracts the validation errors from the exception and constructs a custom error message.
     * The error message is logged using Log4j2 and returned as a ResponseEntity with a BAD_REQUEST status.
     *
     * @param ex The MethodArgumentNotValidException that occurred.
     * @return A ResponseEntity with a BAD_REQUEST status and a custom error message containing the validation errors.
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

        log.error(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * This method handles RabbitMQException.
     * It catches the exception when a RabbitMQ operation fails.
     * It logs the error message and returns a ResponseEntity with a INTERNAL_SERVER_ERROR status and the error message as the body.
     *
     * @param ex The RabbitMQException that occurred. This exception is thrown when a RabbitMQ operation fails.
     * @return A ResponseEntity with a INTERNAL_SERVER_ERROR status and the error message as the body.
     *         This response indicates that the server encountered an unexpected condition that prevented it from fulfilling the request.
     */
    @ExceptionHandler(RabbitMQException.class)
    public ResponseEntity<String> handleRabbitMQException(RabbitMQException ex) {
        log.error("RabbitMQ exception: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
