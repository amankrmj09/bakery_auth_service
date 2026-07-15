package com.blubugtech.bakery_auth_service.exception;

public class InvalidTokenException extends AuthException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
