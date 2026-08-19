package com.codeit.monew.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MonewException.class)
    public ResponseEntity<ErrorResponse> handleMonewException(MonewException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}