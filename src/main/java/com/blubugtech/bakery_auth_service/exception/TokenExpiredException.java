package com.blubugtech.bakery_auth_service.exception;

import com.blubugtech.common.exception.security.InvalidTokenException;

public class TokenExpiredException extends InvalidTokenException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
