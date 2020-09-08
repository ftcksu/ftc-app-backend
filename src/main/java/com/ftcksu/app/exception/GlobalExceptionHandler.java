package com.ftcksu.app.exception;

import com.ftcksu.app.exception.custom.FileNotFoundException;
import com.ftcksu.app.exception.custom.StorageException;
import com.ftcksu.app.exception.exceptionResponse.ErrorResponse;
import com.ftcksu.app.exception.exceptionResponse.SubError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> helperResponse(Exception ex, HttpStatus status, String customMessage) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(new Date())
                .status(status.value())
                .error(status.name())
                .message(customMessage)
                .build();
        return new ResponseEntity(error, status);
    }

    private ResponseEntity<ErrorResponse> helperResponse(Exception ex, HttpStatus status, String customMessage, List<SubError> errors) {
        ResponseEntity<ErrorResponse> response = helperResponse(ex, status, customMessage);
        response.getBody().setErrors(errors);
        return response;
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return helperResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(value = StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException ex) {
        return helperResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }


    @ExceptionHandler(value = FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFoundException(FileNotFoundException ex) {
        return helperResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

//    @ExceptionHandler(value = FileNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleEventCouldNotAddUserException(EventCouldNotAddUserException ex) {
//        return helperResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
//    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String customMessage = String.format("%s  should be of type %s.", ex.getName(), ex.getRequiredType().getSimpleName());
        return helperResponse(ex, HttpStatus.BAD_REQUEST, customMessage);
    }


    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return helperResponse(ex, HttpStatus.BAD_REQUEST, "Malformed JSON request.");
    }


    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String customMessage = ex.getMostSpecificCause().getMessage().replace("key", "field");
        return helperResponse(ex, HttpStatus.CONFLICT, customMessage);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        String customMessage = "Incorrect username or password.";
        return helperResponse(ex, HttpStatus.BAD_REQUEST, customMessage);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleEntityExistsException(EntityExistsException ex) {
        return helperResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String objectName = "";

        for (ConstraintViolation<?> constraintViolation : ex.getConstraintViolations()) {
            objectName = constraintViolation.getRootBeanClass().getName();
            objectName = objectName.substring(objectName.lastIndexOf('.') + 1).toLowerCase();
            break;
        }

        List<SubError> errors = ex.getConstraintViolations().stream()
                .map((e) -> SubError.builder()
                        .field(e.getPropertyPath().toString())
                        .rejectedValue(String.valueOf(e.getInvalidValue()))
                        .message(e.getMessage())
                        .build()
                ).collect(Collectors.toList());

        String message = String.format("Validation failed for object='%s'. Error count: %d.", objectName, errors.size());
        return helperResponse(ex, HttpStatus.BAD_REQUEST, message, errors);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<SubError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map((e) -> SubError.builder()
                        .field(e.getField())
                        .rejectedValue(String.valueOf(e.getRejectedValue()))
                        .message(e.getDefaultMessage())
                        .build()
                ).collect(Collectors.toList());

        String message = String.format("Validation failed for object='%s'. Error count: %d.", ex.getBindingResult().getObjectName(), errors.size());
        return helperResponse(ex, HttpStatus.BAD_REQUEST, message, errors);
    }
}
