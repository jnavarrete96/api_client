package com.api.client.domain.exception;

public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException() {
        super("End date cannot be before start date");
    }

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
