package com.rostik.andrusiv.core.exception;

public class NotRetryableException extends RuntimeException {
    public NotRetryableException(String message) {
        super(message);
    }
}
