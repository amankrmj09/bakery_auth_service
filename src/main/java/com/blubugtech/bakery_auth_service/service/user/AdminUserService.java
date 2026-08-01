package com.blubugtech.bakery_auth_service.service.user;

import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface AdminUserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    Page<UserResponse> searchUsers(String searchTerm, Pageable pageable);
    Page<UserResponse> getUsersByRole(User.Role role, Pageable pageable);
    void updateUserRole(UUID userId, User.Role newRole) throws AuthException;
    void updateUserStatus(UUID userId, User.UserStatus status) throws AuthException;
    void deleteUser(UUID userId) throws AuthException;
    Map<String, Long> getUserStatistics();
}
