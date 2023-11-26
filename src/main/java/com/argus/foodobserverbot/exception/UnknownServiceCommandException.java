package com.argus.foodobserverbot.exception;

public class UnknownServiceCommandException extends RuntimeException{
    public UnknownServiceCommandException(String message) {
        super(message);
    }
}
