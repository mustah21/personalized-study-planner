package com.studyplanner.backend.exception;

import com.studyplanner.backend.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // helper method used only to create a MethodParameter for the exception
    @SuppressWarnings("unused")
    private void dummyMethod(String param) {
        // no-op
    }

    private MethodParameter createMethodParameter() throws NoSuchMethodException {
        Method m = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        return new MethodParameter(m, 0);
    }

    @Test
    void handleValidationExcption_withFieldError_returnsBadRequest() throws NoSuchMethodException {
        MethodParameter param = createMethodParameter();
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "objectName");
        FieldError fieldError = new FieldError("objectName", "email", "must be a well-formed email address");
        bindingResult.addError(fieldError);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ApiResponse<Object>> responseEntity = handler.handleValidationExcption(ex);

        assertNotNull(responseEntity);
        assertEquals(400, responseEntity.getStatusCode().value());
        ApiResponse<Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertTrue(body.getMessage().contains("Validation error"));
        assertTrue(body.getMessage().contains("must be a well-formed email address"));
    }

    @Test
    void handleResourceNotFoundException_returnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        ResponseEntity<ApiResponse<Object>> responseEntity = handler.handleResourceNotFoundException(ex);

        assertEquals(404, responseEntity.getStatusCode().value());
        ApiResponse<Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertTrue(body.getMessage().contains("User not found"));
    }

    @Test
    void handleUnauthorizedAccessExecption_returnsForbidden() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Access denied");
        ResponseEntity<ApiResponse<Object>> responseEntity = handler.handleUnauthorizedAccessExecption(ex);

        assertEquals(403, responseEntity.getStatusCode().value());
        ApiResponse<Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(403, body.getStatus());
        assertTrue(body.getMessage().contains("Access denied"));
    }

    @Test
    void handleIllegalArgumentException_returnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("invalid id");
        ResponseEntity<ApiResponse<Object>> responseEntity = handler.handleIllegalArgumentException(ex);

        assertEquals(400, responseEntity.getStatusCode().value());
        ApiResponse<Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertTrue(body.getMessage().contains("invalid id"));
    }

    @Test
    void handleGeneralException_runtime_returnsInternalServerError() {
        RuntimeException ex = new RuntimeException("db error");
        ResponseEntity<ApiResponse<Object>> responseEntity = handler.handleGeneralException(ex);

        assertEquals(500, responseEntity.getStatusCode().value());
        ApiResponse<Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertTrue(body.getMessage().contains("db error"));
    }

    @Test
    void handleException_genericException_returnsBadRequest() {
        Exception ex = new Exception("something went wrong");
        ResponseEntity<ApiResponse<Object>> responseEntity = handler.handleException(ex);

        assertEquals(400, responseEntity.getStatusCode().value());
        ApiResponse<Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertTrue(body.getMessage().contains("something went wrong"));
    }
}
