package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.*;
import com.blubugtech.bakery_auth_service.exception.AuthException;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request) throws AuthException;

    String initiateRegister(RegisterRequest request) throws AuthException;

    AuthResponse verifyRegister(RegisterVerifyRequest request) throws AuthException;

    String resendRegisterOtp(String email) throws AuthException;

    AuthResponse login(LoginRequest request) throws AuthException;

    com.blubugtech.bakery_auth_service.dto.auth.LoginInitResponse initiateLogin(LoginRequest request) throws AuthException;

    AuthResponse verifyLogin(LoginVerifyRequest request) throws AuthException;

    String resendLoginOtp(String email) throws AuthException;

    com.blubugtech.bakery_auth_service.dto.auth.LoginInitResponse initiateAdminLogin(LoginRequest request) throws AuthException;

    AuthResponse verifyAdminLogin(LoginVerifyRequest request) throws AuthException;

    String initiateForgotPassword(ForgotPasswordRequest request) throws AuthException;

    void resetPassword(ResetPasswordRequest request) throws AuthException;

    AuthResponse refreshToken(String refreshToken) throws AuthException;

    TokenValidationResponse validateToken(String token);

    void logout(String token);

    void changePassword(UUID userId, String currentPassword, String newPassword) throws AuthException;

    void verifyEmail(UUID userId) throws AuthException;
}
