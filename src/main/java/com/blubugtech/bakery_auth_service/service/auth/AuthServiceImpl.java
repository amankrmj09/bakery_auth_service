package com.blubugtech.bakery_auth_service.service.auth;

import com.blubugtech.bakery_auth_service.dto.auth.*;
import com.blubugtech.bakery_auth_service.entity.User;
import com.blubugtech.bakery_auth_service.exception.*;
import com.blubugtech.bakery_auth_service.security.CustomUserDetails;
import com.blubugtech.bakery_auth_service.security.JwtService;
import com.blubugtech.bakery_auth_service.service.user.UserProfileService;
import com.blubugtech.bakery_auth_service.service.user.UserAccountSecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.messaging.constants.KafkaTopics;
import org.blubakery.common.messaging.contract.messaging.UserPayload;
import org.blubakery.common.messaging.event.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.blubugtech.bakery_auth_service.service.auth.UserRegistrationService;
import com.blubugtech.bakery_auth_service.service.auth.UserAuthenticationService;
import com.blubugtech.bakery_auth_service.service.auth.PasswordManagementService;
import com.blubugtech.bakery_auth_service.service.auth.TokenValidationService;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements UserRegistrationService, UserAuthenticationService, PasswordManagementService, TokenValidationService {

    final private UserProfileService userProfileService;
    final private UserAccountSecurityService userAccountSecurityService;

    final private JwtService jwtService;

    final private KafkaTemplate<String, Object> kafkaTemplate;

    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

    private final AuthOtpService authOtpService;
    private final ObjectMapper objectMapper;


    // User registration
    public AuthResponse register(RegisterRequest request) throws AuthException {
        log.info("Processing registration for username: {}", request.getUsername());

        try {
            // Create user through UserService
            User user = userAccountSecurityService.createUser(request);

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            Long expiresIn = jwtService.getExpirationTime();

            log.info("Registration successful for user: {}", user.getUsername());

            // Send welcome notification via Kafka
            try {
                UserPayload payload = UserPayload.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .action("REGISTERED")
                        .phoneNumber(user.getPhone())
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                UserEvent event = new UserEvent();
                event.setEventId(java.util.UUID.randomUUID().toString());
                event.setEventType("USER_REGISTERED");
                event.setTimestamp(java.time.Instant.now());
                event.setPayload(payload);
                kafkaTemplate.send(KafkaTopics.USER_TOPIC, user.getId().toString(), event);
                log.info("Published UserEvent for registered user: {}", user.getId());
            } catch (Exception ex) {
                log.error("Failed to publish UserEvent", ex);
            }

            return AuthResponse.of(accessToken, refreshToken, expiresIn, user);

        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.getUsername(), e);
            throw new AuthException("Registration failed: " + e.getMessage());
        }
    }

    // User login
    public AuthResponse login(LoginRequest request) throws AuthException {
        log.info("Processing login for user: {}", request.getUsernameOrEmail());

        try {
            // Check if account is locked
            if (userAccountSecurityService.isAccountLocked(request.getUsernameOrEmail())) {
                throw new AccountLockedException("Account is locked due to too many failed login attempts. Please try again later.");
            }

            // Authenticate using AuthenticationManager
            org.springframework.security.core.Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                request.getUsernameOrEmail(),
                                request.getPassword()
                        )
                );
            } catch (AuthenticationException e) {
                log.error("Authentication failed for user: {}", request.getUsernameOrEmail(), e);
                // Record failed login attempt
                userAccountSecurityService.recordFailedLogin(request.getUsernameOrEmail());
                throw new InvalidCredentialsException("Invalid credentials");
            }

            // Get user from authentication context
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Record successful login
            userAccountSecurityService.recordSuccessfulLogin(user.getId());

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            Long expiresIn = jwtService.getExpirationTime();

            log.info("Login successful for user: {}", user.getUsername());

            return AuthResponse.of(accessToken, refreshToken, expiresIn, user);

        } catch (AuthException e) {
            log.error("Login failed for user: {}", request.getUsernameOrEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", request.getUsernameOrEmail(), e);
            throw new AuthException("Login failed due to an unexpected error");
        }
    }

    public String initiateRegister(RegisterRequest request) throws AuthException {
        log.info("Initiating OTP registration for: {}", request.getEmail());
        try {
            if (userProfileService.findByUsername(request.getUsername()).isPresent() || userProfileService.findByEmail(request.getEmail()).isPresent()) {
                throw new AuthException("User already exists");
            }
            String requestJson = objectMapper.writeValueAsString(request);
            String otp = authOtpService.generateAndSaveRegisterOtp(request.getEmail(), requestJson);
            sendOtpEvent(null, request.getEmail(), request.getFirstName(), request.getLastName(), request.getPhone(), otp);
            return otp;
        } catch (Exception e) {
            log.error("Failed to initiate registration", e);
            throw new AuthException("Failed to initiate registration");
        }
    }

    public AuthResponse verifyRegister(RegisterVerifyRequest request) throws AuthException {
        try {
            String requestJson = authOtpService.verifyRegisterOtp(request.getEmail(), request.getOtp());
            if (requestJson == null) {
                throw new InvalidTokenException("Invalid or expired OTP");
            }
            RegisterRequest registerRequest = objectMapper.readValue(requestJson, RegisterRequest.class);
            return register(registerRequest); // Re-use existing register flow
        } catch (Exception e) {
            log.error("OTP Verification failed", e);
            throw new AuthException("OTP Verification failed");
        }
    }

    public String resendRegisterOtp(String email) throws AuthException {
        log.info("Resending OTP registration for: {}", email);
        try {
            String otp = authOtpService.resendRegisterOtp(email);
            if (otp == null) {
                throw new InvalidTokenException("Registration session expired or not found. Please register again.");
            }
            sendOtpEvent(null, email, null, null, null, otp);
            return otp;
        } catch (AuthException e) {
            log.error("AuthException while resending registration OTP", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to resend registration OTP", e);
            throw new AuthException("Failed to resend registration OTP");
        }
    }

    public LoginInitResponse initiateLogin(LoginRequest request) throws AuthException {
        try {
            if (userAccountSecurityService.isAccountLocked(request.getUsernameOrEmail())) {
                throw new AccountLockedException("Account locked");
            }
            // Verify password for 2FA
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsernameOrEmail(), request.getPassword()
                        )
                );
            } catch (AuthenticationException e) {
                log.error("Authentication failed", e);
                userAccountSecurityService.recordFailedLogin(request.getUsernameOrEmail());
                throw new InvalidCredentialsException("Invalid credentials");
            }
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (user.getTwoFactorEnabled() != null && !user.getTwoFactorEnabled()) {
                // Bypass 2FA
                userAccountSecurityService.recordSuccessfulLogin(user.getId());

                if (user.getLoginNotificationsEnabled() != null && user.getLoginNotificationsEnabled()) {
                    try {
                        String ipAddress = "Unknown IP";
                        org.springframework.web.context.request.RequestAttributes requestAttributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                        if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                            jakarta.servlet.http.HttpServletRequest httpRequest = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();
                            ipAddress = httpRequest.getHeader("X-Forwarded-For");
                            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                                ipAddress = httpRequest.getRemoteAddr();
                            }
                        }

                        org.blubakery.common.messaging.contract.messaging.UserPayload payload = org.blubakery.common.messaging.contract.messaging.UserPayload.builder()
                                .userId(user.getId())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .action("NEW_SIGN_IN")
                                .ipAddress(ipAddress)
                                .location("Unknown Location")
                                .timestamp(java.time.LocalDateTime.now())
                                .build();
                        org.blubakery.common.messaging.event.UserEvent event = new org.blubakery.common.messaging.event.UserEvent();
                        event.setEventId(java.util.UUID.randomUUID().toString());
                        event.setEventType("NEW_SIGN_IN");
                        event.setTimestamp(java.time.Instant.now());
                        event.setPayload(payload);
                        kafkaTemplate.send(KafkaTopics.USER_TOPIC, user.getId().toString(), event);
                        log.info("Published NEW_SIGN_IN event for user: {}", user.getUsername());
                    } catch (Exception ex) {
                        log.error("Failed to publish NEW_SIGN_IN event for user: {}", user.getUsername(), ex);
                    }
                }

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);
                AuthResponse authResponse = AuthResponse.of(accessToken, refreshToken, jwtService.getExpirationTime(), user);

                return com.blubugtech.bakery_auth_service.dto.auth.LoginInitResponse.builder()
                        .requiresOtp(false)
                        .message("Login successful")
                        .authResponse(authResponse)
                        .build();
            }

            String otp = authOtpService.generateAndSaveLoginOtp(user.getEmail());
            sendOtpEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), otp);

            return LoginInitResponse.builder()
                    .requiresOtp(true)
                    .message(otp)
                    .build();
        } catch (AuthException e) {
            log.error("Auth exception during initiate login", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to initiate login", e);
            throw new AuthException("Failed to initiate login");
        }
    }

    public AuthResponse verifyLogin(LoginVerifyRequest request) throws AuthException {
        Optional<User> userOptional = userProfileService.findByUsername(request.getUsernameOrEmail());
        if (userOptional.isEmpty()) {
            userOptional = userProfileService.findByEmail(request.getUsernameOrEmail());
        }
        if (userOptional.isEmpty()) throw new UserNotFoundException("User not found");
        User user = userOptional.get();

        if (!authOtpService.verifyLoginOtp(user.getEmail(), request.getOtp())) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }

        userAccountSecurityService.recordSuccessfulLogin(user.getId());

        if (user.getLoginNotificationsEnabled() != null && user.getLoginNotificationsEnabled()) {
            try {
                String ipAddress = "Unknown IP";
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes instanceof ServletRequestAttributes) {
                    HttpServletRequest httpRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
                    ipAddress = httpRequest.getHeader("X-Forwarded-For");
                    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                        ipAddress = httpRequest.getRemoteAddr();
                    }
                }

                UserPayload payload = UserPayload.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .action("NEW_SIGN_IN")
                        .ipAddress(ipAddress)
                        .location("Unknown Location") // Typically you'd use a GeoIP service here
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                UserEvent event = new UserEvent();
                event.setEventId(UUID.randomUUID().toString());
                event.setEventType("NEW_SIGN_IN");
                event.setTimestamp(Instant.now());
                event.setPayload(payload);
                kafkaTemplate.send(KafkaTopics.USER_TOPIC, user.getId().toString(), event);
                log.info("Published NEW_SIGN_IN event for user: {}", user.getUsername());
            } catch (Exception ex) {
                log.error("Failed to publish NEW_SIGN_IN event for user: {}", user.getUsername(), ex);
            }
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken, jwtService.getExpirationTime(), user);
    }

    public String resendLoginOtp(String usernameOrEmail) throws AuthException {
        log.info("Resending login OTP for: {}", usernameOrEmail);
        try {
            Optional<User> userOptional = userProfileService.findByUsername(usernameOrEmail);
            if (userOptional.isEmpty()) {
                userOptional = userProfileService.findByEmail(usernameOrEmail);
            }
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }
            User user = userOptional.get();
            String otp = authOtpService.resendLoginOtp(user.getEmail());
            if (otp == null) {
                throw new InvalidTokenException("Login session expired or not found. Please login again.");
            }
            sendOtpEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), otp);
            return otp;
        } catch (AuthException e) {
            log.error("Auth exception during resend login OTP", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to resend login OTP", e);
            throw new AuthException("Failed to resend login OTP");
        }
    }
    public LoginInitResponse initiateAdminLogin(LoginRequest request) throws AuthException {
        log.info("Processing admin login for user: {}", request.getUsernameOrEmail());
        try {
            if (userAccountSecurityService.isAccountLocked(request.getUsernameOrEmail())) {
                throw new AccountLockedException("Account locked");
            }
            
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsernameOrEmail(), request.getPassword()
                        )
                    );
            } catch (AuthenticationException e) {
                log.error("Authentication failed", e);
                userAccountSecurityService.recordFailedLogin(request.getUsernameOrEmail());
                throw new InvalidCredentialsException("Invalid credentials");
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            if (user.getRole() != User.Role.ADMIN) {
                throw new org.blubakery.common.security.exception.security.UnauthorizedAccessException("Access denied. Admin role required.");
            }
            
            String otp = authOtpService.generateAndSaveLoginOtp(user.getEmail());
            sendOtpEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), otp);
            
            return LoginInitResponse.builder()
                    .requiresOtp(true)
                    .message(otp)
                    .build();
        } catch (AuthException e) {
            log.error("Auth exception during initiate admin login", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to initiate admin login", e);
            throw new AuthException("Failed to initiate admin login");
        }
    }

    public AuthResponse verifyAdminLogin(LoginVerifyRequest request) throws AuthException {
        Optional<User> userOptional = userProfileService.findByUsername(request.getUsernameOrEmail());
        if (userOptional.isEmpty()) {
            userOptional = userProfileService.findByEmail(request.getUsernameOrEmail());
        }
        if (userOptional.isEmpty()) throw new UserNotFoundException("User not found");
        User user = userOptional.get();
        
        if (user.getRole() != User.Role.ADMIN) {
            throw new org.blubakery.common.security.exception.security.UnauthorizedAccessException("Access denied. Admin role required.");
        }

        if (!authOtpService.verifyLoginOtp(user.getEmail(), request.getOtp())) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }

        userAccountSecurityService.recordSuccessfulLogin(user.getId());

        if (user.getLoginNotificationsEnabled() != null && user.getLoginNotificationsEnabled()) {
            try {
                String ipAddress = "Unknown IP";
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes instanceof ServletRequestAttributes) {
                    HttpServletRequest httpRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
                    ipAddress = httpRequest.getHeader("X-Forwarded-For");
                    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                        ipAddress = httpRequest.getRemoteAddr();
                    }
                }

                UserPayload payload = UserPayload.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .action("NEW_SIGN_IN")
                        .ipAddress(ipAddress)
                        .location("Unknown Location")
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                UserEvent event = new UserEvent();
                event.setEventId(UUID.randomUUID().toString());
                event.setEventType("NEW_SIGN_IN");
                event.setTimestamp(Instant.now());
                event.setPayload(payload);
                kafkaTemplate.send(KafkaTopics.USER_TOPIC, user.getId().toString(), event);
                log.info("Published NEW_SIGN_IN event for admin user: {}", user.getUsername());
            } catch (Exception ex) {
                log.error("Failed to publish NEW_SIGN_IN event for admin user: {}", user.getUsername(), ex);
            }
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.of(accessToken, refreshToken, jwtService.getExpirationTime(), user);
    }

    public String initiateForgotPassword(ForgotPasswordRequest request) throws AuthException {
        Optional<User> userOpt = userProfileService.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) throw new UserNotFoundException("User not found");
        User user = userOpt.get();
        String otp = authOtpService.generateAndSaveResetOtp(request.getEmail());
        sendOtpEvent(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), otp);
        return otp;
    }

    public void resetPassword(ResetPasswordRequest request) throws AuthException {
        if (!authOtpService.verifyResetOtp(request.getEmail(), request.getOtp())) {
            throw new InvalidTokenException("Invalid or expired OTP");
        }
        Optional<User> userOpt = userProfileService.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) throw new UserNotFoundException("User not found");
        // TODO: For reset, we update without requiring current password. We need a method in userService for direct password update or use existing if it doesn't strictly check old password.
        // We will call the repo directly or add a direct update method to userService.
        User user = userOpt.get();
        // Since we are inside auth service, we can use userAccountSecurityService.resetPassword (assuming we create it) or just update it via another means.
        userAccountSecurityService.resetPassword(user.getId(), request.getNewPassword());
    }

    // Refresh token
    public AuthResponse refreshToken(String refreshToken) throws AuthException {
        log.info("Processing token refresh");

        try {
            // Validate refresh token format
            if (!jwtService.validateToken(refreshToken)) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            // Check if it's actually a refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new InvalidTokenException("Token is not a refresh token");
            }

            // Check if refresh token is expired
            if (jwtService.isTokenExpired(refreshToken)) {
                throw new TokenExpiredException("Refresh token is expired");
            }

            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);

            // Find user
            Optional<User> userOptional = userProfileService.findByUsername(username);
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }

            User user = userOptional.get();

            // Check if user is still active
            if (!user.isActive()) {
                throw new AccountLockedException("Account is not active");
            }

            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            Long expiresIn = jwtService.getExpirationTime();

            log.info("Token refresh successful for user: {}", user.getUsername());

            return AuthResponse.of(newAccessToken, newRefreshToken, expiresIn, user);

        } catch (AuthException e) {
            log.error("Token refresh failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            throw new AuthException("Token refresh failed");
        }
    }

    // Validate token (for other microservices)
    public TokenValidationResponse validateToken(String token) {
        log.debug("Validating token");

        try {
            // Basic token validation
            if (!jwtService.validateToken(token)) {
                return TokenValidationResponse.invalid("Invalid token");
            }

            // Check if it's an access token
            if (!jwtService.isAccessToken(token)) {
                return TokenValidationResponse.invalid("Not an access token");
            }

            // Extract user information
            String username = jwtService.extractUsername(token);
            UUID userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            String email = jwtService.extractEmail(token);

            // Verify user still exists and is active
            Optional<User> userOptional = userProfileService.findById(userId);
            if (userOptional.isEmpty()) {
                return TokenValidationResponse.invalid("User not found");
            }

            User user = userOptional.get();
            if (!user.isActive()) {
                return TokenValidationResponse.invalid("User account is not active");
            }

            log.debug("Token validation successful for user: {}", username);

            return TokenValidationResponse.valid(userId, username, email, role);

        } catch (Exception e) {
            log.error("Token validation error", e);
            return TokenValidationResponse.invalid("Token validation failed");
        }
    }

    // Logout (optional - for token blacklisting if needed)
    public void logout(String token) {
        log.info("Processing logout");

        try {
            String username = jwtService.extractUsername(token);
            log.info("Logout successful for user: {}", username);

            // In a production system, you might want to blacklist the token here
            // For now, we'll just log the logout

        } catch (Exception e) {
            log.error("Logout processing failed", e);
        }
    }

    // Change password (authenticated user)
    public void changePassword(UUID userId, String currentPassword, String newPassword) throws AuthException {
        log.info("Processing password change for user ID: {}", userId);

        try {
            userAccountSecurityService.updatePassword(userId, currentPassword, newPassword);
            log.info("Password change successful for user ID: {}", userId);

            // Send password change notification
            try {
                Optional<User> userOpt = userProfileService.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, Object> notificationReq = new HashMap<>();
                    notificationReq.put("type", "EMAIL");
                    notificationReq.put("recipientEmail", user.getEmail());
                    org.blubakery.common.messaging.contract.messaging.UserPayload payload = org.blubakery.common.messaging.contract.messaging.UserPayload.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .action("PASSWORD_CHANGED")
                            .phoneNumber(user.getPhone())
                            .timestamp(java.time.LocalDateTime.now())
                            .build();
                    UserEvent event = new UserEvent();
                    event.setEventId(java.util.UUID.randomUUID().toString());
                    event.setEventType("PASSWORD_CHANGED");
                    event.setTimestamp(java.time.Instant.now());
                    event.setPayload(payload);
                    kafkaTemplate.send(KafkaTopics.USER_TOPIC, user.getId().toString(), event);
                    log.info("Published UserEvent for password change: {}", user.getId());
                }
            } catch (Exception ex) {
                log.error("Failed to send password change notification", ex);
            }

        } catch (Exception e) {
            log.error("Password change failed for user ID: {}", userId, e);
            throw new AuthException("Password change failed: " + e.getMessage());
        }
    }

    // Verify email (if you implement email verification)
    public void verifyEmail(UUID userId) throws AuthException {
        log.info("Processing email verification for user ID: {}", userId);

        try {
            userAccountSecurityService.verifyEmail(userId);
            log.info("Email verification successful for user ID: {}", userId);

        } catch (Exception e) {
            log.error("Email verification failed for user ID: {}", userId, e);
            throw new AuthException("Email verification failed");
        }
    }


    private void sendOtpEvent(UUID userId, String email, String firstName, String lastName, String phone, String otp) {
        try {
            org.blubakery.common.messaging.contract.messaging.UserPayload payload = org.blubakery.common.messaging.contract.messaging.UserPayload.builder()
                    .userId(userId)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .action("OTP_REQUESTED")
                    .phoneNumber(phone)
                    .otpCode(otp)
                    .expiryMinutes(10) // Fixed matching AuthOtpService validity
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            UserEvent event = new UserEvent();
            event.setEventId(java.util.UUID.randomUUID().toString());
            event.setEventType("OTP_REQUESTED");
            event.setTimestamp(java.time.Instant.now());
            event.setPayload(payload);

            String key = userId != null ? userId.toString() : email;
            kafkaTemplate.send(KafkaTopics.USER_TOPIC, key, event);
            log.info("Published UserEvent for OTP request to email: {}", email);
        } catch (Exception ex) {
            log.error("Failed to publish OTP event", ex);
        }
    }
}
