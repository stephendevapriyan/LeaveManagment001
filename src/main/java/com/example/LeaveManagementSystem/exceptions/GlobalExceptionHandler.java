package com.example.LeaveManagementSystem.exceptions;

import com.example.LeaveManagementSystem.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .message("Unexpected error occurred: "+ ex.getMessage())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .data(null)
                        .build());
    }

    @ExceptionHandler(IdNotFoundException.class)
    public ResponseEntity<ApiResponse> handleIdNotFoundException(IdNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .message(ex.getMessage())
                        .status(HttpStatus.NO_CONTENT.value())
                        .data(null)
                        .build());
    }
}