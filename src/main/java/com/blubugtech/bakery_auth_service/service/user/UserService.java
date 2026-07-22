package com.blubugtech.bakery_auth_service.service.user;

import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest request) throws AuthException;
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    Optional<User> findById(UUID userId);
    Optional<User> findByUsername(String username);
    UserResponse getUserProfile(UUID userId) throws AuthException;
    UserResponse updateUserProfile(UUID userId, RegisterRequest request) throws AuthException;
    void updatePassword(UUID userId, String oldPassword, String newPassword) throws AuthException;
    void resetPassword(UUID userId, String newPassword) throws AuthException;
    void recordSuccessfulLogin(UUID userId) throws AuthException;
    void recordFailedLogin(String usernameOrEmail);
    boolean isAccountLocked(String usernameOrEmail);
    void unlockAccount(UUID userId) throws AuthException;
    List<UserResponse> getAllUsers();
    List<UserResponse> searchUsers(String searchTerm);
    List<UserResponse> getUsersByRole(User.Role role);
    void updateUserRole(UUID userId, User.Role newRole) throws AuthException;
    void updateUserStatus(UUID userId, User.UserStatus status) throws AuthException;
    void verifyEmail(UUID userId) throws AuthException;
    void deleteUser(UUID userId) throws AuthException;
    Map<String, Long> getUserStatistics();
    boolean userExists(String usernameOrEmail);
    boolean validateCredentials(String usernameOrEmail, String password);
}
