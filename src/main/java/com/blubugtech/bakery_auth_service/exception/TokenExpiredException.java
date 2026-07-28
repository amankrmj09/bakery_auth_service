package com.blubugtech.bakery_auth_service.exception;

import org.blubakery.bakery_common_libs.exception.security.InvalidTokenException;

public class TokenExpiredException extends InvalidTokenException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
