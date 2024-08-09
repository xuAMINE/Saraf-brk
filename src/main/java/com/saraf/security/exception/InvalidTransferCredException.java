package com.saraf.security.exception;

public class InvalidTransferCredException extends RuntimeException {

    public InvalidTransferCredException(String message) {
        super(message);
    }

    public InvalidTransferCredException(String message, Throwable cause) {
        super(message, cause);
    }
}
