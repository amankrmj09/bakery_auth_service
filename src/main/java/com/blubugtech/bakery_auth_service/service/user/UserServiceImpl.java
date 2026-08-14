package com.blubugtech.bakery_auth_service.service.user;

import com.blubugtech.bakery_auth_service.dto.auth.RegisterRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserProfileUpdateRequest;
import com.blubugtech.bakery_auth_service.dto.user.UserResponse;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.AuthException;
import com.blubugtech.bakery_auth_service.mapper.UserMapper;
import com.blubugtech.bakery_auth_service.repository.UserRepository;
import com.blubugtech.bakery_auth_service.service.dashboard.DashboardStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.blubakery.common.core.dto.RestPageResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserProfileService, UserAccountSecurityService, AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DashboardStatisticsService dashboardStatisticsService;
    private final UserMapper userMapper;

    @Value("${security.login.max-attempts:5}")
    private Integer maxLoginAttempts;

    @Value("${security.login.lockout-duration:300000}") // 5 minutes default
    private Long lockoutDuration;

    // Create new user
    public User createUser(RegisterRequest request) throws AuthException {
        log.info("Creating new user with username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already exists");
        }

        // Check if phone already exists
        if (request.getPhone() != null && !request.getPhone().isEmpty() &&
                userRepository.existsByPhone(request.getPhone())) {
            throw new AuthException("Phone number already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(User.Role.USER); // Default role
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setLoginAttempts(0);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        dashboardStatisticsService.incrementUsers();

        return savedUser;
    }

    // Find user by username or email
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    // Find user by ID
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    // Find user by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get user profile
    public UserResponse getUserProfile(UUID userId) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        return userMapper.toResponse(user);
    }

    // Update user profile
    public UserResponse updateUserProfile(UUID userId, UserProfileUpdateRequest request) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already exists");
        }

        // Check if new username is already taken by another user
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already exists");
        }

        // Check if new phone is already taken by another user
        if (request.getPhone() != null && !request.getPhone().isEmpty() &&
                !request.getPhone().equals(user.getPhone()) &&
                userRepository.existsByPhone(request.getPhone())) {
            throw new AuthException("Phone number already exists");
        }

        // Update user details
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        if (request.getTwoFactorEnabled() != null) {
            user.setTwoFactorEnabled(request.getTwoFactorEnabled());
        }
        if (request.getLoginNotificationsEnabled() != null) {
            user.setLoginNotificationsEnabled(request.getLoginNotificationsEnabled());
        }

        // If email changed, mark as unverified
        if (!user.getEmail().equals(request.getEmail())) {
            user.setEmailVerified(false);
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated for ID: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    // Update user password
    public void updatePassword(UUID userId, String oldPassword, String newPassword) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password updated for user ID: {}", userId);
    }

    public void resetPassword(UUID userId, String newPassword) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for user ID: {}", userId);
    }

    // Record successful login
    public void recordSuccessfulLogin(UUID userId) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        user.setLoginAttempts(0); // Reset failed attempts
        user.setLockedUntil(null); // Unlock account if locked
        userRepository.save(user);

        log.info("Successful login recorded for user: {}", user.getUsername());
    }

    // Record failed login attempt
    public void recordFailedLogin(String usernameOrEmail) {
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(usernameOrEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            int attempts = user.getLoginAttempts() + 1;
            user.setLoginAttempts(attempts);

            // Lock account if max attempts reached
            if (attempts >= maxLoginAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusSeconds(lockoutDuration / 1000));
                log.warn("Account locked for user: {} after {} failed attempts",
                        user.getUsername(), attempts);
            }

            userRepository.save(user);
            log.info("Failed login attempt recorded for user: {} (Attempt: {})",
                    user.getUsername(), attempts);
        }
    }

    // Check if account is locked
    public boolean isAccountLocked(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
                .map(User::isAccountLocked)
                .orElse(false);
    }

    // Unlock user account (admin function)
    public void unlockAccount(UUID userId) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        user.setLockedUntil(null);
        user.setLoginAttempts(0);
        userRepository.save(user);

        log.info("Account unlocked for user: {}", user.getUsername());
    }

    // Get all users (admin function)
    public RestPageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable)
                .map(userMapper::toResponse);
        return new RestPageResponse<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    // Search users
    public RestPageResponse<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        Page<UserResponse> page = userRepository.searchUsers(searchTerm, pageable)
                .map(userMapper::toResponse);
        return new RestPageResponse<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    // Get users by role
    public RestPageResponse<UserResponse> getUsersByRole(User.Role role, Pageable pageable) {
        Page<UserResponse> page = userRepository.findByRole(role, pageable)
                .map(userMapper::toResponse);
        return new RestPageResponse<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    // Update user role (admin function)
    public void updateUserRole(UUID userId, User.Role newRole) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        user.setRole(newRole);
        userRepository.save(user);
        log.info("Role updated to {} for user: {}", newRole, user.getUsername());
    }

    // Update user status (admin function)
    public void updateUserStatus(UUID userId, User.UserStatus status) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        user.setStatus(status);
        userRepository.save(user);
        log.info("Status updated to {} for user: {}", status, user.getUsername());
    }

    // Verify email
    public void verifyEmail(UUID userId) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getUsername());
    }

    // Delete user (admin function)
    public void deleteUser(UUID userId) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        userRepository.delete(user);
        dashboardStatisticsService.decrementUsers();
        log.info("User deleted: {}", user.getUsername());
    }

    // Get user statistics
    @org.springframework.cache.annotation.Cacheable(value = "user_statistics")
    public com.blubugtech.bakery_auth_service.dto.user.UserStatisticsResponse getUserStatistics() {
        Object[] stats = userRepository.getUserStatistics();
        com.blubugtech.bakery_auth_service.dto.user.UserStatisticsResponse response = new com.blubugtech.bakery_auth_service.dto.user.UserStatisticsResponse();

        if (stats.length > 0) {
            Object[] row = (Object[]) stats[0];
            response.setTotalUsers(((Number) row[0]).longValue());
            response.setTOTAL_USERS(((Number) row[0]).longValue()); // Added for frontend compatibility
            response.setActiveUsers(((Number) row[1]).longValue());
            response.setVerifiedUsers(((Number) row[2]).longValue());
            response.setAdminUsers(((Number) row[3]).longValue());
        }

        return response;
    }

    // Check if user exists
    public boolean userExists(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail).isPresent();
    }

    // Validate user credentials
    public boolean validateCredentials(String usernameOrEmail, String password) {
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(usernameOrEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.isActive() && passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
}
