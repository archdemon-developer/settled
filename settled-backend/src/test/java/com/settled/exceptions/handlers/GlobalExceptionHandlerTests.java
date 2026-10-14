package com.settled.exceptions.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.settled.enums.ErrorCode;
import com.settled.exceptions.DuplicateResourceException;
import com.settled.exceptions.ResourceNotFoundException;
import com.settled.models.ResponseWrapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTests {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void shouldHandleResourceNotFoundException() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException(ErrorCode.ACCOUNT_NOT_FOUND, "Account with ID abc-123 not found");

        ResponseEntity<ResponseWrapper<?>> response = globalExceptionHandler.handleSettledException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertEquals(404, response.getBody().getCode());
        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND.getCode(),
                response.getBody().getError().getErrorCode());
        assertEquals(
                "Account with ID abc-123 not found",
                response.getBody().getError().getDetails());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleDuplicateResourceException() {

        DuplicateResourceException ex = new DuplicateResourceException(
                ErrorCode.DUPLICATE_ACCOUNT_CODE, "Code 'ACC001' already exists", "code");

        ResponseEntity<ResponseWrapper<?>> response = globalExceptionHandler.handleSettledException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertEquals(409, response.getBody().getCode());
        assertEquals(
                ErrorCode.DUPLICATE_ACCOUNT_CODE.getCode(),
                response.getBody().getError().getErrorCode());
        assertEquals(
                "Code 'ACC001' already exists", response.getBody().getError().getDetails());
        assertEquals("code", response.getBody().getError().getField());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Database connection failed");

        ResponseEntity<ResponseWrapper<?>> response = globalExceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertEquals(500, response.getBody().getCode());
        assertEquals("INTERNAL_ERROR", response.getBody().getError().getErrorCode());
        assertEquals(
                "An unexpected error occurred", response.getBody().getError().getMessage());
        assertNull(response.getBody().getError().getDetails());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getValue()).thenReturn("not-a-uuid");

        ResponseEntity<ResponseWrapper<?>> response = globalExceptionHandler.handleTypeMismatchException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertEquals(400, response.getBody().getCode());
        assertEquals(
                ErrorCode.INVALID_PARAMETER.getCode(),
                response.getBody().getError().getErrorCode());
        assertEquals("id", response.getBody().getError().getField());
        assertEquals(
                "Parameter 'id' has an invalid value: 'not-a-uuid'",
                response.getBody().getError().getDetails());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ResponseWrapper<?>> response = globalExceptionHandler.handleUnreadableBodyException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertEquals(400, response.getBody().getCode());
        assertEquals(
                ErrorCode.MALFORMED_REQUEST_BODY.getCode(),
                response.getBody().getError().getErrorCode());
        assertEquals(
                "Request body is missing, is not valid JSON, or contains a value of the wrong type",
                response.getBody().getError().getDetails());
        assertNull(response.getBody().getError().getField());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("createRequest", "code", "must not be blank");
        FieldError fieldError2 = new FieldError("createRequest", "name", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ResponseWrapper<?>> response = globalExceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertEquals(400, response.getBody().getCode());
        assertEquals(
                ErrorCode.VALIDATION_ERROR.getCode(),
                response.getBody().getError().getErrorCode());
        assertEquals(2, response.getBody().getError().getViolations().size());
        assertEquals(
                "code", response.getBody().getError().getViolations().get(0).getField());
        assertEquals(
                "must not be blank",
                response.getBody().getError().getViolations().get(0).getMessage());
        assertNotNull(response.getBody().getRequestId());
        assertNotNull(response.getBody().getTimestamp());
    }
}
