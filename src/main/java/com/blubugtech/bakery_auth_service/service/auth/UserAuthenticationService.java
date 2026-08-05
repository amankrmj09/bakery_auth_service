package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.AuthResponse;
import com.blubugtech.bakery_auth_service.dto.auth.LoginInitResponse;
import com.blubugtech.bakery_auth_service.dto.auth.LoginRequest;
import com.blubugtech.bakery_auth_service.dto.auth.LoginVerifyRequest;
import com.blubugtech.bakery_auth_service.exception.AuthException;

public interface UserAuthenticationService {
    AuthResponse login(LoginRequest request) throws AuthException;

    LoginInitResponse initiateLogin(LoginRequest request) throws AuthException;

    AuthResponse verifyLogin(LoginVerifyRequest request) throws AuthException;

    String resendLoginOtp(String email) throws AuthException;

    LoginInitResponse initiateAdminLogin(LoginRequest request) throws AuthException;

    AuthResponse verifyAdminLogin(LoginVerifyRequest request) throws AuthException;

    void logout(String token);
}
