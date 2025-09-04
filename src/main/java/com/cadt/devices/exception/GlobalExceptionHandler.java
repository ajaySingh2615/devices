package com.cadt.devices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> api(ApiException e) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(e.code(), e.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> val(MethodArgumentNotValidException e) {
        var m = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", m));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointer(NullPointerException e) {
        // Log the actual error for debugging
        System.err.println("[ERROR] NullPointerException: " + e.getMessage());
        e.printStackTrace();
        
        // Return user-friendly message
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("AUTHENTICATION_ERROR", "Authentication failed. Please try again."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e) {
        // Log the actual error for debugging
        System.err.println("[ERROR] RuntimeException: " + e.getMessage());
        e.printStackTrace();
        
        // Return user-friendly message
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("SERVER_ERROR", "Something went wrong. Please try again."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        // Log the actual error for debugging
        System.err.println("[ERROR] Unexpected error: " + e.getMessage());
        e.printStackTrace();
        
        // Return user-friendly message
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("UNKNOWN_ERROR", "An unexpected error occurred. Please contact support."));
    }
}
