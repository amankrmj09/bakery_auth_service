package com.blubugtech.bakery_auth_service.exception;

import org.blubakery.common.security.exception.security.InvalidTokenException;

public class TokenExpiredException extends InvalidTokenException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
