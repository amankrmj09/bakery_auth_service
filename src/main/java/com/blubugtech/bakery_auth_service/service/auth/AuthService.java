package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.AuthResponse;
import com.blubugtech.bakery_auth_service.dto.auth.LoginRequest;
import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.dto.auth.TokenValidationResponse;
import com.blubugtech.bakery_auth_service.exception.AuthException;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request) throws AuthException;
    AuthResponse login(LoginRequest request) throws AuthException;
    AuthResponse refreshToken(String refreshToken) throws AuthException;
    TokenValidationResponse validateToken(String token);
    void logout(String token);
    void changePassword(UUID userId, String currentPassword, String newPassword) throws AuthException;
    void verifyEmail(UUID userId) throws AuthException;
}
