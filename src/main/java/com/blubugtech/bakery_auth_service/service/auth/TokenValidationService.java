package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.AuthResponse;
import com.blubugtech.bakery_auth_service.dto.auth.TokenValidationResponse;
import com.blubugtech.bakery_auth_service.exception.AuthException;

public interface TokenValidationService {
    AuthResponse refreshToken(String refreshToken) throws AuthException;

    TokenValidationResponse validateToken(String token);
}
