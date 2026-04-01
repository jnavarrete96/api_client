package com.api.client.domain.exception;

public class DuplicateClientException extends RuntimeException {
    public DuplicateClientException(String email) {
        super("Client with email already exists: " + email);
    }
}
