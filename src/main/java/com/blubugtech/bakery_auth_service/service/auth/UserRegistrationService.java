package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.AuthResponse;
import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.dto.auth.RegisterVerifyRequest;
import com.blubugtech.bakery_auth_service.exception.AuthException;

public interface UserRegistrationService {
    AuthResponse register(RegisterRequest request) throws AuthException;

    String initiateRegister(RegisterRequest request) throws AuthException;

    AuthResponse verifyRegister(RegisterVerifyRequest request) throws AuthException;

    String resendRegisterOtp(String email) throws AuthException;

    void verifyEmail(java.util.UUID userId) throws AuthException;
}
