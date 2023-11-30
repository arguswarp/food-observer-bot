package com.argus.foodobserverbot.exception;

public class EmptyUpdateException extends RuntimeException {
    public EmptyUpdateException(String message) {
        super(message);
    }
}
