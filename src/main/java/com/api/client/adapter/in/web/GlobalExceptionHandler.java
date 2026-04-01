package com.api.client.adapter.in.web;

import com.api.client.domain.exception.DuplicateClientException;
import com.api.client.domain.exception.InvalidDateRangeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateClientException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicate(DuplicateClientException ex) {
        log.warn("Duplicate client: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidDateRange(InvalidDateRangeException ex) {
        log.warn("Invalid date range: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred");
    }
}