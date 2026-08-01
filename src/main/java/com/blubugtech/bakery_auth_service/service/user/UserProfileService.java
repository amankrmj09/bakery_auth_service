package com.blubugtech.bakery_auth_service.service.user;

import com.blubugtech.bakery_auth_service.dto.user.UserProfileUpdateRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileService {
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    Optional<User> findById(UUID userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    UserResponse getUserProfile(UUID userId) throws AuthException;
    UserResponse updateUserProfile(UUID userId, UserProfileUpdateRequest request) throws AuthException;
    boolean userExists(String usernameOrEmail);
}
