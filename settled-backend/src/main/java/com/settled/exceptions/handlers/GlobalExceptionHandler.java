package com.settled.exceptions.handlers;

import com.settled.enums.ErrorCode;
import com.settled.exceptions.SettledException;
import com.settled.models.ErrorDetails;
import com.settled.models.FieldViolation;
import com.settled.models.ResponseWrapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SettledException.class)
    public ResponseEntity<ResponseWrapper<?>> handleSettledException(SettledException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ErrorDetails errorDetails = ErrorDetails.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(ex.getDetails())
                .field(ex.getField())
                .build();

        ResponseWrapper<?> response = ResponseWrapper.builder()
                .status("ERROR")
                .code(errorCode.getHttpStatus().value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .error(errorDetails)
                .build();

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<?>> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldViolation.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        ErrorDetails errorDetails = ErrorDetails.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .violations(violations)
                .build();

        ResponseWrapper<?> response = ResponseWrapper.builder()
                .status("ERROR")
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .error(errorDetails)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseWrapper<?>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;

        ErrorDetails errorDetails = ErrorDetails.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(String.format("Parameter '%s' has an invalid value: '%s'", ex.getName(), ex.getValue()))
                .field(ex.getName())
                .build();

        ResponseWrapper<?> response = ResponseWrapper.builder()
                .status("ERROR")
                .code(errorCode.getHttpStatus().value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .error(errorDetails)
                .build();

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapper<?>> handleUnreadableBodyException(HttpMessageNotReadableException ex) {

        ErrorCode errorCode = ErrorCode.MALFORMED_REQUEST_BODY;

        ErrorDetails errorDetails = ErrorDetails.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details("Request body is missing, is not valid JSON, or contains a value of the wrong type")
                .build();

        ResponseWrapper<?> response = ResponseWrapper.builder()
                .status("ERROR")
                .code(errorCode.getHttpStatus().value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .error(errorDetails)
                .build();

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<?>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .build();

        ResponseWrapper<?> response = ResponseWrapper.builder()
                .status("ERROR")
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .error(errorDetails)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
