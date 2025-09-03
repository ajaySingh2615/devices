package com.cadt.devices.exception;

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
}
