package com.shah_s.bakery_auth_service.exception;

import org.devofblue.common.exception.InvalidTokenException;

public class TokenExpiredException extends InvalidTokenException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
