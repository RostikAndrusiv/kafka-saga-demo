package com.rostik.andrusiv.core.exception;

public class RetriableException extends RuntimeException {
    public RetriableException(String message) {
        super(message);
    }
}
