package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.ForgotPasswordRequest;
import com.blubugtech.bakery_auth_service.dto.auth.ResetPasswordRequest;
import com.blubugtech.bakery_auth_service.exception.AuthException;

import java.util.UUID;

public interface PasswordManagementService {
    String initiateForgotPassword(ForgotPasswordRequest request) throws AuthException;

    void resetPassword(ResetPasswordRequest request) throws AuthException;

    void changePassword(UUID userId, String currentPassword, String newPassword) throws AuthException;
}
