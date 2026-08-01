package com.blubugtech.bakery_auth_service.service.user;

import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;

import java.util.UUID;

public interface UserAccountSecurityService {
    User createUser(RegisterRequest request) throws AuthException;
    void updatePassword(UUID userId, String oldPassword, String newPassword) throws AuthException;
    void resetPassword(UUID userId, String newPassword) throws AuthException;
    void recordSuccessfulLogin(UUID userId) throws AuthException;
    void recordFailedLogin(String usernameOrEmail);
    boolean isAccountLocked(String usernameOrEmail);
    void unlockAccount(UUID userId) throws AuthException;
    void verifyEmail(UUID userId) throws AuthException;
    boolean validateCredentials(String usernameOrEmail, String password);
}
